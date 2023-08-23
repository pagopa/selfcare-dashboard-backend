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
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.core.UserGroupService;
import it.pagopa.selfcare.dashboard.web.config.ExchangeTokenProperties;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
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
import org.springframework.util.StringUtils;

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
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExchangeTokenService {

    private static final String PRIVATE_KEY_HEADER_TEMPLATE = "-----BEGIN %s-----";
    private static final String PRIVATE_KEY_FOOTER_TEMPLATE = "-----END %s-----";

    private final PrivateKey jwtSigningKey;
    private final JwtService jwtService;
    private final Duration duration;
    private final String kid;
    private final InstitutionService institutionService;
    private final UserGroupService groupService;
    private final ProductsConnector productsConnector;
    private final String issuer;

    public ExchangeTokenService(JwtService jwtService,
                                InstitutionService institutionService,
                                UserGroupService groupService,
                                ProductsConnector productConnector,
                                ExchangeTokenProperties properties) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.jwtService = jwtService;
        this.productsConnector = productConnector;
        this.institutionService = institutionService;
        this.groupService = groupService;
        this.jwtSigningKey = getPrivateKey(properties.getSigningKey());
        this.issuer = properties.getIssuer();
        this.duration = Duration.parse(properties.getDuration());
        this.kid = properties.getKid();
    }


    public ExchangedToken exchange(String institutionId, String productId, Optional<String> environment) {
        log.trace("exchange start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "exchange institutionId = {}, productId = {}", institutionId, productId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is required");
        }
        final ProductGrantedAuthority productGrantedAuthority = authentication.getAuthorities()
                .stream()
                .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                .map(SelfCareGrantedAuthority.class::cast)
                .filter(grantedAuthority -> institutionId.equals(grantedAuthority.getInstitutionId()))
                .map(SelfCareGrantedAuthority::getRoleOnProducts)
                .filter(map -> map.containsKey(productId))
                .map(map -> map.get(productId))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("A Product Granted SelfCareAuthority is required for product '%s' and institution '%s'", productId, institutionId)));
        Claims selcClaims = jwtService.getClaims(authentication.getCredentials().toString());
        Assert.notNull(selcClaims, "Session token claims is required");
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        Assert.notNull(institutionInfo, "Institution info is required");
        SelfCareUser principal = (SelfCareUser) authentication.getPrincipal();
        TokenExchangeClaims claims = new TokenExchangeClaims(selcClaims);
        claims.setId(UUID.randomUUID().toString());
        claims.setIssuer(issuer);
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
        institution.setRoles(productGrantedAuthority.getProductRoles().stream()
                .map(productRoleCode -> {
                    Role role = new Role();
                    role.setPartyRole(productGrantedAuthority.getPartyRole());
                    role.setProductRole(productRoleCode);
                    return role;
                }).collect(Collectors.toList()));
        Page<UserGroupInfo> groupInfos = groupService.getUserGroups(Optional.of(institutionId),
                Optional.of(productId),
                Optional.of(UUID.fromString(principal.getId())),
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
                    .collect(Collectors.toList()));
        }
        claims.setInstitution(institution);
        Product product = productsConnector.getProduct(productId);
        environment.ifPresentOrElse(env -> claims.setAudience(product.getBackOfficeEnvironmentConfigurations().get(env).getIdentityTokenAudience())
                , () -> claims.setAudience(product.getIdentityTokenAudience()));
        claims.setDesiredExpiration(claims.getExpiration());
        claims.setIssuedAt(new Date());
        claims.setExpiration(Date.from(claims.getIssuedAt().toInstant().plus(duration)));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "Exchanged claims = {}", claims);
        String jwts = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.RS256, jwtSigningKey)
                .setHeaderParam(JwsHeader.KEY_ID, kid)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .compact();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "Exchanged token = {}", jwts);
        final String urlBO = environment.map(env -> product.getBackOfficeEnvironmentConfigurations().get(env).getUrl())
                .orElse(product.getUrlBO());
        log.trace("exchange end");
        return new ExchangedToken(jwts, urlBO);
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
    static class Role implements Serializable {
        private PartyRole partyRole;
        @JsonProperty("role")
        private String productRole;
    }


    static class TokenExchangeClaims extends DefaultClaims {
        public static final String DESIRED_EXPIRATION = "desired_exp";
        public static final String INSTITUTION = "organization";

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

    }

}
