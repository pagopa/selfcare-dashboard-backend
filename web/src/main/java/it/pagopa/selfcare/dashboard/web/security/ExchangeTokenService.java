package it.pagopa.selfcare.dashboard.web.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.core.UserGroupV2Service;
import it.pagopa.selfcare.dashboard.core.UserService;
import it.pagopa.selfcare.dashboard.web.config.ExchangeTokenProperties;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.product.entity.Product;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class ExchangeTokenService {

    private static final String PRIVATE_KEY_HEADER_TEMPLATE = "-----BEGIN %s-----";
    private static final String PRIVATE_KEY_FOOTER_TEMPLATE = "-----END %s-----";

    private final String billingUrl;
    private final String billingAudience;
    private final PrivateKey jwtSigningKey;
    private final JwtService jwtService;
    private final Duration duration;
    private final String kid;
    private final InstitutionService institutionService;
    private final UserGroupV2Service groupService;
    public final UserService userService;
    private final ProductsConnector productsConnector;
    private final String issuer;

    public ExchangeTokenService(JwtService jwtService,
                                InstitutionService institutionService,
                                UserGroupV2Service groupService,
                                ProductsConnector productConnector,
                                ExchangeTokenProperties properties,
                                UserService userService) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.billingUrl = properties.getBillingUrl();
        this.billingAudience = properties.getBillingAudience();
        this.jwtService = jwtService;
        this.productsConnector = productConnector;
        this.institutionService = institutionService;
        this.groupService = groupService;
        this.jwtSigningKey = getPrivateKey(properties.getSigningKey());
        this.issuer = properties.getIssuer();
        this.duration = Duration.parse(properties.getDuration());
        this.kid = properties.getKid();
        this.userService = userService;
    }


    public ExchangedToken exchange(String institutionId, String productId, Optional<String> environment) {
        log.trace("exchange start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "exchange institutionId = {}, productId = {}", institutionId, productId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is required");
        }

        List<Map<String, ProductGrantedAuthority>> productGrantedAuthorityMap = retrieveProductGrantedAuthorityMap(authentication, institutionId);
        final ProductGrantedAuthority productGrantedAuthority = productGrantedAuthorityMap.stream()
                .filter(map -> map.containsKey(productId))
                .map(map -> map.get(productId))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("A Product Granted SelfCareAuthority is required for product '%s' and institution '%s'", productId, institutionId)));

        Institution institution = retrieveInstitution(institutionId, List.of(productGrantedAuthority), false);
        SelfCareUser principal = (SelfCareUser) authentication.getPrincipal();
        retrieveAndSetGroups(institution, institutionId, productId, principal);
        TokenExchangeClaims claims = retrieveAndSetClaims(authentication.getCredentials().toString(), institution, principal);

        Product product = productsConnector.getProduct(productId);

        environment.ifPresentOrElse(env -> claims.setAudience(product.getBackOfficeEnvironmentConfigurations().get(env).getIdentityTokenAudience())
                , () -> claims.setAudience(product.getIdentityTokenAudience()));

        log.debug(LogUtils.CONFIDENTIAL_MARKER, "Exchanged claims = {}", claims);
        String jwts = createJwts(claims);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "Exchanged token = {}", jwts);

        final String urlBO = environment.map(env -> product.getBackOfficeEnvironmentConfigurations().get(env).getUrl())
                .orElse(product.getUrlBO());

        log.trace("exchange end");
        return new ExchangedToken(jwts, urlBO);
    }

    public ExchangedToken retrieveBillingExchangedToken(String institutionId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is required");
        }

        List<String> invoiceableProductList = retrieveInvoiceableProductList();

        final List<ProductGrantedAuthority> productGrantedAuthorities = new ArrayList<>();

        List<Map<String, ProductGrantedAuthority>> productGrantedAuthorityMap = retrieveProductGrantedAuthorityMap(authentication, institutionId);
        productGrantedAuthorityMap.forEach(stringProductGrantedAuthorityMap -> addProductIfIsInvoiceable(stringProductGrantedAuthorityMap, invoiceableProductList, productGrantedAuthorities));

        Institution institution = retrieveInstitution(institutionId, productGrantedAuthorities, true);
        SelfCareUser principal = (SelfCareUser) authentication.getPrincipal();
        retrieveAndSetGroups(institution, institutionId, null, principal);

        TokenExchangeClaims claims = retrieveAndSetClaims(authentication.getCredentials().toString(), institution, principal);
        claims.setAudience(billingAudience);

        log.debug(LogUtils.CONFIDENTIAL_MARKER, "Exchanged claims = {}", claims);
        String jwts = createJwts(claims);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "Exchanged token = {}", jwts);
        log.trace("exchange end");

        return new ExchangedToken(jwts, billingUrl);
    }

    private List<String> retrieveInvoiceableProductList() {
        return productsConnector.getProductsTree()
                .stream()
                .flatMap(productTree -> {
                    Stream<Product> nodeStream = Stream.of(productTree.getNode());
                    Stream<Product> childrenStream = productTree.getChildren() != null
                            ? productTree.getChildren().stream()
                            : Stream.empty();
                    return Stream.concat(nodeStream, childrenStream);
                })
                .filter(Product::isInvoiceable)
                .map(Product::getId)
                .toList();
    }

    private TokenExchangeClaims retrieveAndSetClaims(String credential, Institution institution, SelfCareUser principal) {
        Claims selcClaims = jwtService.getClaims(credential);
        Assert.notNull(selcClaims, "Session token claims is required");
        TokenExchangeClaims claims = new TokenExchangeClaims(selcClaims);
        claims.setId(UUID.randomUUID().toString());
        claims.setIssuer(issuer);
        User user = userService.getUserByInternalId(UUID.fromString(principal.getId()));

        String email = Optional.ofNullable(user.getWorkContact(institution.getId()))
                .map(workContract -> Objects.nonNull(workContract.getEmail())
                        ? workContract.getEmail().getValue()
                        : ""
                )
                .orElse(null);

        claims.setEmail(email);
        claims.setInstitution(institution);
        claims.setDesiredExpiration(claims.getExpiration());
        claims.setIssuedAt(new Date());
        claims.setExpiration(Date.from(claims.getIssuedAt().toInstant().plus(duration)));

        return claims;
    }


    private List<Map<String, ProductGrantedAuthority>> retrieveProductGrantedAuthorityMap(Authentication authentication, String institutionId) {
        return authentication.getAuthorities()
                .stream()
                .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                .map(SelfCareGrantedAuthority.class::cast)
                .filter(grantedAuthority -> institutionId.equals(grantedAuthority.getInstitutionId()))
                .map(SelfCareGrantedAuthority::getRoleOnProducts)
                .toList();
    }


    private String createJwts(TokenExchangeClaims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.RS256, jwtSigningKey)
                .setHeaderParam(JwsHeader.KEY_ID, kid)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .compact();
    }

    private void retrieveAndSetGroups(Institution institution, String institutionId, String productId, SelfCareUser principal) {
        Page<UserGroupInfo> groupInfos = groupService.getUserGroups(institutionId, productId, UUID.fromString(principal.getId()),
                Pageable.ofSize(100));// 100 is a reasonably safe number to retrieve all groups related to a generic user
        if (groupInfos.hasNext()) {
            log.warn(String.format("Current user (%s) is member of more than 100 groups related to institution %s and product %s. The Identity Token will contain only the first 100 records",
                    principal.getId(),
                    institutionId,
                    productId));
        }
        if (!groupInfos.isEmpty()) {
            institution.setGroups(groupInfos.stream()
                    .map(UserGroupInfo::getId)
                    .toList());
        }
    }

    private Institution retrieveInstitution(String institutionId, List<ProductGrantedAuthority> productGrantedAuthorities, boolean isBillingToken) {
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        Assert.notNull(institutionInfo, "Institution info is required");
        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setName(institutionInfo.getDescription());
        institution.setTaxCode(institutionInfo.getTaxCode());
        institution.setSubUnitType(institutionInfo.getSubunitType());
        institution.setSubUnitCode(institutionInfo.getSubunitCode());
        institution.setAooParent(institutionInfo.getAooParentCode());
        institution.setParentDescription(institutionInfo.getParentDescription());
        RootParent rootParent = new RootParent();
        rootParent.setId(institutionInfo.getRootParentId());
        rootParent.setDescription(institutionInfo.getParentDescription());
        institution.setRootParent(rootParent);
        institution.setOriginId(institutionInfo.getOriginId());
        institution.setRoles(retrieveInstitutionRoles(productGrantedAuthorities, isBillingToken));
        return institution;
    }

    private List<Role> retrieveInstitutionRoles(List<ProductGrantedAuthority> productGrantedAuthorities, boolean isBillingToken) {
        List<Role> roles = new ArrayList<>();

        for (ProductGrantedAuthority authority : productGrantedAuthorities) {
            roles.addAll(constructRole(authority, isBillingToken));
        }
        return roles;
    }

    private List<Role> constructRole(ProductGrantedAuthority productGrantedAuthority, boolean isBillingToken) {
        return productGrantedAuthority.getProductRoles().stream()
                .map(productRoleCode -> {
                    Role role = new Role();
                    role.setPartyRole(productGrantedAuthority.getPartyRole());
                    role.setProductRole(productRoleCode);
                    if (isBillingToken) {
                        role.setProductId(productGrantedAuthority.getProductId());
                    }
                    return role;
                }).toList();
    }

    private void addProductIfIsInvoiceable(Map<String, ProductGrantedAuthority> map, List<String> productList, List<ProductGrantedAuthority> productGrantedAuthorities) {
        map.keySet()
                .forEach(key -> productList.stream().filter(key::equalsIgnoreCase)
                        .findFirst()
                        .ifPresent(productId -> productGrantedAuthorities.add(map.get(key))));
    }


    private PrivateKey getPrivateKey(String signingKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        boolean isRsa = signingKey.contains("RSA");
        String privateKeyEnvelopName = (isRsa ? "RSA " : "") + "PRIVATE KEY";
        String privateKeyPEM = signingKey
                .replace("\r", "")
                .replace("\n", "")
                .replace(String.format(PRIVATE_KEY_HEADER_TEMPLATE, privateKeyEnvelopName), "")
                .replace(String.format(PRIVATE_KEY_FOOTER_TEMPLATE, privateKeyEnvelopName), "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeySpec keySpec;
        if (isRsa) {
            RSAPrivateKey rsaPrivateKey = RSAPrivateKey.getInstance(encoded);
            keySpec = new RSAPrivateCrtKeySpec(
                    rsaPrivateKey.getModulus(),
                    rsaPrivateKey.getPublicExponent(),
                    rsaPrivateKey.getPrivateExponent(),
                    rsaPrivateKey.getPrime1(),
                    rsaPrivateKey.getPrime2(),
                    rsaPrivateKey.getExponent1(),
                    rsaPrivateKey.getExponent2(),
                    rsaPrivateKey.getCoefficient());

        } else {
            keySpec = new PKCS8EncodedKeySpec(encoded);
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }


    @Data
    @ToString
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Institution implements Serializable {
        private String id;
        @JsonProperty("fiscal_code")
        private String taxCode;
        private String name;
        private List<Role> roles;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> groups;
        private String subUnitCode;
        private String subUnitType;
        private String aooParent;
        @Deprecated
        private String parentDescription;
        @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = RootParent.class)
        private RootParent rootParent;
        @JsonProperty("ipaCode")
        private String originId;
    }

    @Data
    static class RootParent implements Serializable {
        private String id;
        private String description;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Role implements Serializable {
        private PartyRole partyRole;
        @JsonProperty("role")
        private String productRole;
        private String productId;
    }


    static class TokenExchangeClaims extends DefaultClaims {
        public static final String DESIRED_EXPIRATION = "desired_exp";
        public static final String INSTITUTION = "organization";
        public static final String EMAIL = "email";

        public TokenExchangeClaims(Map<String, Object> map) {
            super(map);
        }

        public Claims setDesiredExpiration(Date desiredExp) {
            setDate(DESIRED_EXPIRATION, desiredExp);
            return this;
        }

        public Claims setInstitution(Institution institution) {
            setValue(INSTITUTION, institution);
            return this;
        }

        public Claims setEmail(String email) {
            setValue(EMAIL, email);
            return this;
        }

    }

}
