package it.pagopa.selfcare.dashboard.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.commons.web.security.JwtService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

@Slf4j
@Service
public class ExchangeTokenService {

    private static final String ISSUER = "api.selfcare.pagopa.it";
    private static final String PRIVATE_KEY_HEADER_TEMPLATE = "-----BEGIN %s-----";
    private static final String PRIVATE_KEY_FOOTER_TEMPLATE = "-----END %s-----";

    private final PrivateKey jwtSigningKey;
    private final JwtService jwtService;
    private final Duration duration;

    public ExchangeTokenService(
            JwtService jwtService,
            @Value("${jwt.exchange.signingKey}") String jwtSigningKey,
            @Value("${jwt.exchange.duration}") String duration) throws Exception {
        this.jwtService = jwtService;
        this.jwtSigningKey = getPrivateKey(jwtSigningKey);
        this.duration = Duration.parse(duration);
    }

    public String exchange(String productCode, String realm) {
        if (log.isDebugEnabled()) {
            log.trace("ExchangeTokenService.exchange");
            log.debug("productCode = " + productCode + ", realm = " + realm);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException();
        }
        //TODO: retrieve product role from authentication object
        String institutionId = ((SelfCareAuthenticationDetails) authentication.getDetails()).getInstitutionId();
        Optional<Claims> selcClaims = jwtService.getClaims(authentication.getCredentials().toString());
        if (selcClaims.isEmpty()) {
            throw new RuntimeException();//TODO: define a custom exception
        }
        System.out.println("selcClaims.get().setIssuedAt() = " + selcClaims.get().getIssuedAt());//FIXME:remove
        TokenExchangeClaims claims = new TokenExchangeClaims(selcClaims.get());
        claims.setId(UUID.randomUUID().toString());
        claims.setAudience(realm);
        claims.setIssuer(ISSUER);
        claims.setInstitution(new Institution(institutionId, null));
        claims.setDesiredExpiration(claims.getExpiration());
        claims.setIssuedAt(new Date());
        claims.setExpiration(Date.from(claims.getIssuedAt().toInstant().plus(duration)));
        System.out.println("claims.getIssuedAt() = " + claims.getIssuedAt());//FIXME:remove

        if (log.isDebugEnabled()) {
            log.debug("Exchanged claims: " + claims.toString());
        }

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.RS512, jwtSigningKey)
                .compact();
    }


    private PrivateKey getPrivateKey(String signingKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        boolean isRsa = signingKey.contains("RSA");
        String privateKeyEnvelopName = (isRsa ? "RSA " : "") + "PRIVATE KEY";
        String privateKeyPEM = signingKey
                .replaceAll("\\r", "")
                .replaceAll("\\n", "")
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


    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    static class Institution implements Serializable {
        private String id;
        private String role;
    }


    static class TokenExchangeClaims extends DefaultClaims {
        public static final String DESIRED_EXPIRATION = "desired_exp";
        public static final String INSTITUTION = "company";

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
