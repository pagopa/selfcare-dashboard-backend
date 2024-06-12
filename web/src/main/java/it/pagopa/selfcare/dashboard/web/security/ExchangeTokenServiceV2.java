package it.pagopa.selfcare.dashboard.web.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.OnboardedProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitution;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.core.UserGroupV2Service;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.config.ExchangeTokenProperties;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapper;
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

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
public class ExchangeTokenServiceV2 {

    private static final String PRIVATE_KEY_HEADER_TEMPLATE = "-----BEGIN %s-----";
    private static final String PRIVATE_KEY_FOOTER_TEMPLATE = "-----END %s-----";
    private static final String ID = "ID";
    private final String billingUrl;
    private final String billingAudience;
    private final PrivateKey jwtSigningKey;
    private final JwtService jwtService;
    private final Duration duration;
    private final String kid;
    private final InstitutionService institutionService;
    private final UserGroupV2Service groupService;
    public final UserV2Service userService;
    public final UserApiConnector userApiConnector;

    private final ProductsConnector productsConnector;
    private final String issuer;

    private final InstitutionResourceMapper institutionResourceMapper;

    public ExchangeTokenServiceV2(JwtService jwtService,
                                  InstitutionService institutionService,
                                  UserGroupV2Service groupService,
                                  ProductsConnector productConnector,
                                  ExchangeTokenProperties properties,
                                  UserV2Service userService,
                                  UserApiConnector userApiConnector,
                                  InstitutionResourceMapper institutionResourceMapper)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
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
        this.userApiConnector = userApiConnector;
        this.institutionResourceMapper = institutionResourceMapper;
    }


    public ExchangedToken exchange(String institutionId, String productId, Optional<String> environment) {
        log.trace("exchange start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "exchange institutionId = {}, productId = {}", institutionId, productId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is required");
        }
        SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
        String userId = selfCareUser.getId();
        UserInstitution userInstitution = userApiConnector.getProducts(institutionId, userId);

        Map<String, ProductGrantedAuthority> productGrantedAuthorityMap = retrieveProductsFromInstitutionAndUser(userInstitution);
        final ProductGrantedAuthority productGrantedAuthority = Optional.ofNullable(productGrantedAuthorityMap.get(productId))
                .orElseThrow(() -> new IllegalArgumentException(String.format("A Product Granted SelfCareAuthority is required for product '%s' and institution '%s'", productId, institutionId)));


        it.pagopa.selfcare.dashboard.connector.model.institution.Institution institution = institutionService.getInstitutionById(institutionId);
        Assert.notNull(institution, "Institution info is required");
        ExchangeTokenServiceV2.Institution institutionExchange = institutionResourceMapper.toInstitution(institution, List.of(productGrantedAuthority), false);
        retrieveAndSetGroups(institutionExchange, institutionId, productId, userId);
        TokenExchangeClaims claims = retrieveAndSetClaims(authentication.getCredentials().toString(), institutionExchange, userId, userInstitution.getUserMailUuid());

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
        if (authentication == null || authentication.getPrincipal() == null || authentication.getCredentials() == null) {
            throw new IllegalStateException("Authentication is required");
        }

        SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
        String userId = selfCareUser.getId();
        String credentials = authentication.getCredentials().toString();

        List<String> invoiceableProductList = retrieveInvoiceableProductList();

        final List<ProductGrantedAuthority> productGrantedAuthorities = new ArrayList<>();

        UserInstitution userInstitution = userApiConnector.getProducts(institutionId, userId);

        Map<String, ProductGrantedAuthority> productGrantedAuthorityMap = retrieveProductsFromInstitutionAndUser(userInstitution);
        addProductIfIsInvoiceable(productGrantedAuthorityMap, invoiceableProductList, productGrantedAuthorities);
        it.pagopa.selfcare.dashboard.connector.model.institution.Institution institutionInfo = institutionService.getInstitutionById(institutionId);
        Assert.notNull(institutionInfo, "Institution info is required");
        ExchangeTokenServiceV2.Institution institution = institutionResourceMapper.toInstitution(institutionInfo, productGrantedAuthorities, true);

        retrieveAndSetGroups(institution, institutionId, null, userId);

        TokenExchangeClaims claims = retrieveAndSetClaims(credentials, institution, userId, userInstitution.getUserMailUuid());
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

    private TokenExchangeClaims retrieveAndSetClaims(String credential, Institution institution, String userId, String userMailUuid) {
        Claims selcClaims = jwtService.getClaims(credential);
        Assert.notNull(selcClaims, "Session token claims is required");
        TokenExchangeClaims claims = new TokenExchangeClaims(selcClaims);
        claims.setId(UUID.randomUUID().toString());
        claims.setIssuer(issuer);
        User user = userService.getUserById(userId, null, null);

        String email = Optional.ofNullable(user.getWorkContact(userMailUuid))
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
        claims.setSubject(UUID.fromString(userId).toString());
        claims.setType(ID);

        return claims;
    }


    private Map<String, ProductGrantedAuthority> retrieveProductsFromInstitutionAndUser(UserInstitution userInstitution) {
        Map<String, ProductGrantedAuthority> map = new HashMap<>();
        userInstitution
                .getProducts()
                .stream()
                .filter(product -> product.getStatus().equals(RelationshipState.ACTIVE))
                .collect(groupingBy(OnboardedProduct::getProductId))
                .forEach((key, value) -> map.put(key, new ProductGrantedAuthority(
                        value.get(0).getRole(),
                        value.stream().map(OnboardedProduct::getProductRole).toList(),
                        key)));
        return map;
    }


    private String createJwts(TokenExchangeClaims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.RS256, jwtSigningKey)
                .setHeaderParam(JwsHeader.KEY_ID, kid)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .compact();
    }

    private void retrieveAndSetGroups(Institution institution, String institutionId, String productId, String userId) {
        Page<UserGroupInfo> groupInfos = groupService.getUserGroups(institutionId, productId, UUID.fromString(userId),
                Pageable.ofSize(100));// 100 is a reasonably safe number to retrieve all groups related to a generic user
        if (groupInfos.hasNext()) {
            log.warn(String.format("Current user (%s) is member of more than 100 groups related to institution %s and product %s. The Identity Token will contain only the first 100 records",
                    userId,
                    institutionId,
                    productId));
        }
        if (!groupInfos.isEmpty()) {
            institution.setGroups(groupInfos.stream()
                    .map(UserGroupInfo::getId)
                    .toList());
        }
    }

    private void addProductIfIsInvoiceable(
            Map<String, ProductGrantedAuthority> map,
            List<String> productList,
            List<ProductGrantedAuthority> productGrantedAuthorities
    ) {
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
    public static class Institution implements Serializable {
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
    public static class RootParent implements Serializable {
        private String id;
        private String description;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Role implements Serializable {
        private PartyRole partyRole;
        @JsonProperty("role")
        private String productRole;
        private String productId;
    }


    static class TokenExchangeClaims extends DefaultClaims {
        public static final String DESIRED_EXPIRATION = "desired_exp";
        public static final String INSTITUTION = "organization";
        public static final String EMAIL = "email";
        public static final String TYPE = "typ";

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

        public Claims setType(String type) {
            setValue(TYPE, type);
            return this;
        }

    }

}
