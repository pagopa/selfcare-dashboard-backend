package it.pagopa.selfcare.dashboard.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExchangeTokenServiceTest {

    @BeforeEach
    void cleanContext() {
        TestSecurityContextHolder.clearContext();
    }


    @Test
    void exchange_illegalBase64Signature() {
        // given
        String jwtSigningKey = "invalid signature";
        // when
        Executable executable = () -> new ExchangeTokenService(null, null, jwtSigningKey, null, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertTrue(e.getMessage().startsWith("Illegal base64"));
    }


    @Test
    void exchange_cannotParsePKCS8Key() {
        // given
        String jwtSigningKey = "-----BEGIN PRIVATE KEY-----"
                + Base64.getEncoder().encodeToString("invalid signature".getBytes())
                + "-----END PRIVATE KEY-----";
        // when
        Executable executable = () -> new ExchangeTokenService(null, null, jwtSigningKey, null, null);
        // then
        assertThrows(InvalidKeySpecException.class, executable);
    }


    @Test
    void exchange_cannotParsePKCS1Key() {
        // given
        String jwtSigningKey = "-----BEGIN RSA PRIVATE KEY-----"
                + Base64.getEncoder().encodeToString("invalid signature".getBytes())
                + "-----END RSA PRIVATE KEY-----";
        // when
        Executable executable = () -> new ExchangeTokenService(null, null, jwtSigningKey, null, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertTrue(e.getMessage().startsWith("failed to construct sequence from byte[]"));
    }


    @Test
    void exchange_noAuth() throws Exception {
        // given
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        JwtService jwtServiceMock = Mockito.mock(JwtService.class);
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, null, jwtSigningKey, "PT5S", null);
        // when
        Executable executable = () -> exchangeTokenService.exchange(null, null, null);
        // then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals("Authentication is required", e.getMessage());
        Mockito.verifyNoInteractions(jwtServiceMock);
    }


    @Test
    void exchange_noSelfCareAuth() throws Exception {
        // given
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        JwtService jwtServiceMock = Mockito.mock(JwtService.class);
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, null, jwtSigningKey, "PT5S", null);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password");
        TestSecurityContextHolder.setAuthentication(authentication);
        // when
        Executable executable = () -> exchangeTokenService.exchange(null, null, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Self Care Granted SelfCareAuthority is required", e.getMessage());
        Mockito.verifyNoInteractions(jwtServiceMock);
    }


    @Test
    void exchange_noSessionTokenClaims() throws Exception {
        // given
        String institutionId = "institutionId";
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        JwtService jwtServiceMock = Mockito.mock(JwtService.class);
        Mockito.when(jwtServiceMock.getClaims(Mockito.any()))
                .thenReturn(null);
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, null, jwtSigningKey, "PT5S", null);
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(SelfCareAuthority.ADMIN, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        // when
        Executable executable = () -> exchangeTokenService.exchange(institutionId, null, null);
        // then
        RuntimeException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Session token claims is required", e.getMessage());
        Mockito.verify(jwtServiceMock, Mockito.times(1))
                .getClaims(Mockito.any());
        Mockito.verifyNoMoreInteractions(jwtServiceMock);
    }


    @Test
    void exchange_noInstitutionInfo() throws Exception {
        // given
        String institutionId = "institutionId";
        String jti = "id";
        String sub = "subject";
        Date iat = Date.from(Instant.now().minusSeconds(1));
        Date exp = Date.from(iat.toInstant().plusSeconds(5));
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        JwtService jwtServiceMock = Mockito.mock(JwtService.class);
        Mockito.when(jwtServiceMock.getClaims(Mockito.any()))
                .thenReturn(Jwts.claims()
                        .setId(jti)
                        .setSubject(sub)
                        .setIssuedAt(iat)
                        .setExpiration(exp));
        InstitutionService institutionServiceMock = Mockito.mock(InstitutionService.class);
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, institutionServiceMock, jwtSigningKey, "PT5S", null);
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(SelfCareAuthority.ADMIN, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        // when
        Executable executable = () -> exchangeTokenService.exchange(institutionId, null, null);
        // then
        RuntimeException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Institution info is required", e.getMessage());
        Mockito.verify(jwtServiceMock, Mockito.times(1))
                .getClaims(Mockito.any());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock);
    }


    @ParameterizedTest
    @EnumSource(PrivateKey.class)
    void exchange_ok(PrivateKey privateKey) throws Exception {
        // given
        String realm = "realm";
        String jti = "id";
        String sub = "subject";
        Date iat = Date.from(Instant.now().minusSeconds(1));
        Date exp = Date.from(iat.toInstant().plusSeconds(5));
        String institutionId = "institutionId";
        String productId = "productId";
        String productRole = "productRole";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(SelfCareAuthority.ADMIN, productRole, productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        JwtService jwtServiceMock = Mockito.mock(JwtService.class);
        Mockito.when(jwtServiceMock.getClaims(Mockito.any()))
                .thenReturn(Jwts.claims()
                        .setId(jti)
                        .setSubject(sub)
                        .setIssuedAt(iat)
                        .setExpiration(exp));
        InstitutionService institutionServiceMock = Mockito.mock(InstitutionService.class);
        InstitutionInfo institutionInfo = TestUtils.mockInstance(new InstitutionInfo());
        Mockito.when(institutionServiceMock.getInstitution(Mockito.any()))
                .thenReturn(institutionInfo);
        File file = ResourceUtils.getFile(privateKey.getResourceLocation());
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        String kid = "kid";
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, institutionServiceMock, jwtSigningKey, "PT5S", kid);
        // when
        String token = exchangeTokenService.exchange(institutionId, productId, realm);
        // then
        assertNotNull(token);
        Jws<Claims> claimsJws = Jwts.parser()
                .setSigningKey(loadPublicKey())
                .parseClaimsJws(token);
        assertNotNull(claimsJws);
        assertNotNull(claimsJws.getHeader());
        assertEquals(kid, claimsJws.getHeader().getKeyId());
        TestTokenExchangeClaims exchangedClaims = new TestTokenExchangeClaims(claimsJws.getBody());
        assertNotEquals(jti, exchangedClaims.getId());
        assertNotEquals(0, exp.compareTo(exchangedClaims.getExpiration()));
        assertEquals(sub, exchangedClaims.getSubject());
        assertEquals("api.selfcare.pagopa.it", exchangedClaims.getIssuer());
        assertEquals(realm, exchangedClaims.getAudience());
        // https://github.com/jwtk/jjwt/issues/122:
        // The JWT RFC *mandates* NumericDate values are represented as seconds.
        // Because java.util.Date requires milliseconds, we need to multiply by 1000:
        assertEquals(exp.toInstant().getEpochSecond(), exchangedClaims.getDesiredExpiration().toInstant().getEpochSecond());
        assertTrue(exchangedClaims.getIssuedAt().after(iat));
        assertTrue(exchangedClaims.getExpiration().after(exp));
        assertTrue(exchangedClaims.getExpiration().after(exchangedClaims.getIssuedAt()));
        ExchangeTokenService.Institution institution = exchangedClaims.getInstitution();
        assertNotNull(institution);
        assertEquals(institutionId, institution.getId());
        assertEquals(productRole, institution.getRole());
        assertEquals(institutionInfo.getTaxCode(), institution.getTaxCode());
        Mockito.verify(jwtServiceMock, Mockito.times(1))
                .getClaims(Mockito.any());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock);
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
            ExchangeTokenService.Institution institution = new ExchangeTokenService.Institution();
            institution.setId(o.get("id"));
            institution.setRole(o.get("role"));
            institution.setTaxCode(o.get("fiscal_code"));
            return institution;
        }
    }


    @Getter
    private enum PrivateKey {
        PKCS1("classpath:certs/PKCS1Key.pem"),
        PKCS8("classpath:certs/PKCS8key.pem");

        private String resourceLocation;

        PrivateKey(String resourceLocation) {
            this.resourceLocation = resourceLocation;
        }
    }

}