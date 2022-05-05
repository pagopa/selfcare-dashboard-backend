package it.pagopa.selfcare.dashboard.web.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.core.UserGroupService;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
                                @Value("${jwt.exchange.signingKey}") String jwtSigningKey,
                                @Value("${jwt.exchange.duration}") String duration,
                                @Value("${jwt.exchange.kid}") String kid,
                                @Value("${jwt.exchange.issuer}") String issuer
    ) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.jwtService = jwtService;
        this.productsConnector = productConnector;
        this.institutionService = institutionService;
        this.groupService = groupService;
        this.jwtSigningKey = getPrivateKey(jwtSigningKey);
        this.issuer = issuer;
        this.duration = Duration.parse(duration);
        this.kid = kid;
    }


    public String exchange(String institutionId, String productId, String realm) {
        log.trace("exchange start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "exchange institutionId = {}, productId = {}, realm = {}", institutionId, productId, realm);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is required");
        }
        Optional<? extends GrantedAuthority> selcAuthority = authentication.getAuthorities()
                .stream()
                .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                .map(SelfCareGrantedAuthority.class::cast)
                .filter(grantedAuthority -> institutionId.equals(grantedAuthority.getInstitutionId()))
                .findAny();
        SelfCareGrantedAuthority grantedAuthority = (SelfCareGrantedAuthority) selcAuthority
                .orElseThrow(() -> new IllegalArgumentException("A Self Care Granted SelfCareAuthority is required"));
        Claims selcClaims = jwtService.getClaims(authentication.getCredentials().toString());
        Assert.notNull(selcClaims, "Session token claims is required");
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        Assert.notNull(institutionInfo, "Institution info is required");
        SelfCareUser principal = (SelfCareUser) authentication.getPrincipal();
        Collection<UserGroupInfo> groupInfos = groupService.getUserGroups(Optional.of(institutionId), Optional.of(productId), Optional.of(UUID.fromString(principal.getId())), Pageable.unpaged());
        TokenExchangeClaims claims = new TokenExchangeClaims(selcClaims);
        claims.setId(UUID.randomUUID().toString());
        claims.setAudience(realm);
        claims.setIssuer(issuer);
        Institution institution = new Institution();
        institution.setId(institutionId);
        institution.setTaxCode(institutionInfo.getTaxCode());
        Product product = productsConnector.getProduct(productId);
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = product.getRoleMappings();

        List<Role> roles = new ArrayList<>();

        grantedAuthority.getRoleOnProducts().get(productId).getProductRoles().forEach(productRoleCode -> {
            Role role = new Role();
            role.setPartyRole(Product.getPartyRole(productRoleCode, roleMappings).orElse(null));
            role.setRole(productRoleCode);
            roles.add(role);
        });
        institution.setRoles(roles);
        claims.setInstitution(institution);
        claims.setDesiredExpiration(claims.getExpiration());
        claims.setIssuedAt(new Date());
        claims.setExpiration(Date.from(claims.getIssuedAt().toInstant().plus(duration)));
        if (!groupInfos.isEmpty()) {
            List<String> groupIds = groupInfos.stream()
                    .map(UserGroupInfo::getId)
                    .collect(Collectors.toList());
            claims.setGroupIds(groupIds);
        }
        String result = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.RS256, jwtSigningKey)
                .setHeaderParam(JwsHeader.KEY_ID, kid)
                .compact();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "Exchanged claims = {}", claims);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "Exchanged token = {}", result);
        log.trace("exchange end");
        return result;
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
        private List<Role> roles;
    }

    @Data
    static class Role implements Serializable {
        private PartyRole partyRole;
        private String role;
    }

    static class TokenExchangeClaims extends DefaultClaims {
        public static final String DESIRED_EXPIRATION = "desired_exp";
        public static final String INSTITUTION = "organization";
        public static final String GROUP_IDS = "groups";

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

        public Claims setGroupIds(List<String> groupIds) {
            setValue(GROUP_IDS, groupIds);
            return this;
        }
    }

}
