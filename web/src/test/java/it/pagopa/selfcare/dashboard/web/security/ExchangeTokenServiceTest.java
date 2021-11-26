package it.pagopa.selfcare.dashboard.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.commons.web.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeTokenServiceTest {

    @Test
    void exchange_cannotParseSignature() throws Exception {
        // given
        String jwtSigningKey = "invalid signature";
        // when
        Executable executable = () -> new ExchangeTokenService(null, jwtSigningKey, null);
        // then
        assertThrows(InvalidKeySpecException.class, executable);
    }

    @Test
    void exchange_authNotSet() throws Exception {
        // given
        JwtService jwtServiceMock = Mockito.mock(JwtService.class);
        File file = ResourceUtils.getFile("classpath:certs/key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, jwtSigningKey, "PT5S");
        // when
        Executable executable = () -> exchangeTokenService.exchange(null, null);
        // then
        assertThrows(RuntimeException.class, executable);
        Mockito.verifyNoInteractions(jwtServiceMock);
    }


    @Test
    void exchange_ok() throws Exception {
        // given
        String realm = "realm";
        String jti = "id";
        String sub = "subject";
        Date iat = new Date();
        Date exp = new Date();
        String institutionId = "institutionId";
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password");
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        TestSecurityContextHolder.setAuthentication(authentication);
        JwtService jwtServiceMock = Mockito.mock(JwtService.class);
        Claims selcClaims = Jwts.claims()
                .setId(jti)
                .setSubject(sub)
                .setIssuedAt(iat)
                .setExpiration(exp);
        Mockito.when(jwtServiceMock.getClaims(Mockito.any()))
                .thenReturn(Optional.of(selcClaims));
        File file = ResourceUtils.getFile("classpath:certs/key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, jwtSigningKey, "PT5S");
        // when
        String token = exchangeTokenService.exchange(null, realm);
        // then
        assertNotNull(token);
        TestTokenExchangeClaims exchangedClaims =
                new TestTokenExchangeClaims(Jwts.parser()
                        .setSigningKey(loadPublicKey())
                        .parseClaimsJws(token)
                        .getBody());
        assertNotEquals(jti, exchangedClaims.getId());
        assertNotEquals(0, exp.compareTo(exchangedClaims.getExpiration()));
        assertEquals(sub, exchangedClaims.getSubject());
        assertEquals("api.selfcare.pagopa.it", exchangedClaims.getIssuer());
        assertEquals(realm, exchangedClaims.getAudience());
        // https://github.com/jwtk/jjwt/issues/122:
        // The JWT RFC *mandates* NumericDate values are represented as seconds.
        // Because Because java.util.Date requires milliseconds, we need to multiply by 1000:
        assertEquals(exp.toInstant().getEpochSecond(), exchangedClaims.getDesiredExpiration().toInstant().getEpochSecond());
        assertTrue(exchangedClaims.getIssuedAt().after(iat));
        assertTrue(exchangedClaims.getExpiration().after(exp));
        assertTrue(exchangedClaims.getExpiration().after(exchangedClaims.getIssuedAt()));
        ExchangeTokenService.Institution institution = exchangedClaims.getInstitution();
        assertNotNull(institution);
        assertEquals(institutionId, institution.getId());
        assertEquals(null, institution.getRole());//FIXME
        Mockito.verify(jwtServiceMock, Mockito.times(1))
                .getClaims(Mockito.any());
        Mockito.verifyNoMoreInteractions(jwtServiceMock);
    }


    private PublicKey loadPublicKey() throws Exception {
        File file = ResourceUtils.getFile("classpath:certs/pubkey.pem");
        String key = Files.readString(file.toPath(), Charset.defaultCharset());

        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.getMimeDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }


    private static class TestTokenExchangeClaims extends ExchangeTokenService.TokenExchangeClaims {

        public TestTokenExchangeClaims(Map<String, Object> map) {
            super(map);
        }

        public Date getDesiredExpiration() {
            return getDate(DESIRED_EXPIRATION);
        }

        public ExchangeTokenService.Institution getInstitution() {
            LinkedHashMap<String, String> o = (LinkedHashMap) get(INSTITUTION);
            return new ExchangeTokenService.Institution(o.get("id"), o.get("role"));
        }
    }

}