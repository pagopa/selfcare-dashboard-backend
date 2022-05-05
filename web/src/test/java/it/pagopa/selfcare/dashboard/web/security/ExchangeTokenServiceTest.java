package it.pagopa.selfcare.dashboard.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.core.UserGroupService;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.util.ResourceUtils;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

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

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
class ExchangeTokenServiceTest {

    @BeforeEach
    void cleanContext() {
        TestSecurityContextHolder.clearContext();
    }


    @SystemStub
    private EnvironmentVariables environmentVariables;


    @Test
    void exchange_illegalBase64Signature() {
        // given
        String jwtSigningKey = "invalid signature";
        // when
        Executable executable = () -> new ExchangeTokenService(null, null, null, null, jwtSigningKey, null, null, null);
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
        Executable executable = () -> new ExchangeTokenService(null, null, null, null, jwtSigningKey, null, null, null);
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
        Executable executable = () -> new ExchangeTokenService(null, null, null, null, jwtSigningKey, null, null, null);
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
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, null, null, null, jwtSigningKey, "PT5S", null, null);
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
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, null, null, null, jwtSigningKey, "PT5S", null, null);
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
    void exchange_SelfCareAuthOnDifferentInstId() throws Exception {
        // given
        String institutionId = "institutionId";
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        JwtService jwtServiceMock = Mockito.mock(JwtService.class);
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, null, null, null, jwtSigningKey, "PT5S", null, null);
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(SelfCareAuthority.ADMIN, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority("institutionId2", roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        // when
        Executable executable = () -> exchangeTokenService.exchange(institutionId, null, null);
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
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, null, null, null, jwtSigningKey, "PT5S", null, null);
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
        ProductsConnector productsConnectorMock = Mockito.mock(ProductsConnector.class);
        UserGroupService groupServiceMock = Mockito.mock(UserGroupService.class);
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, jwtSigningKey, "PT5S", null, null);
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
    void exchange_nullGroupInfo(PrivateKey privateKey) throws Exception {
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
        UUID userId = UUID.randomUUID();
        SelfCareUser selfCareUser = SelfCareUser.builder(userId.toString()).email("test@example.com").build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(selfCareUser, "password", authorities);

        ProductsConnector productsConnectorMock = Mockito.mock(ProductsConnector.class);
        Product product = TestUtils.mockInstance(new Product());
        ProductRoleInfo productRoleInfo = TestUtils.mockInstance(new ProductRoleInfo());
        ProductRoleInfo.ProductRole productRole1 = TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1, "setCode");
        productRole1.setCode(productRole);
        productRoleInfo.setRoles(List.of(productRole1));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<PartyRole, ProductRoleInfo>(PartyRole.class);
        roleMappings.put(PartyRole.OPERATOR, productRoleInfo);
        product.setRoleMappings(roleMappings);

        Mockito.when(productsConnectorMock.getProduct(Mockito.anyString()))
                .thenReturn(product);

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
        UserGroupService groupServiceMock = Mockito.mock(UserGroupService.class);
        Mockito.when(groupServiceMock.getUserGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        File file = ResourceUtils.getFile(privateKey.getResourceLocation());
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        String kid = "kid";
        environmentVariables.set("JWT_TOKEN_EXCHANGE_ISSUER", "https://dev.selfcare.pagopa.it");
        String issuer = "https://dev.selfcare.pagopa.it";
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, jwtSigningKey, "PT5S", kid, issuer);
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
        assertEquals(issuer, exchangedClaims.getIssuer());
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
        assertEquals(institutionInfo.getTaxCode(), institution.getTaxCode());
        assertNotNull(institution.getRoles());
        assertEquals(1, institution.getRoles().size());
        assertFalse(exchangedClaims.containsKey("groups"));
        Mockito.verify(jwtServiceMock, Mockito.times(1))
                .getClaims(Mockito.any());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitution(institutionId);
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .getUserGroups(Optional.of(institutionId), Optional.of(productId), Optional.of(userId), Pageable.unpaged());
        Mockito.verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock, groupServiceMock);
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
        UUID userId = UUID.randomUUID();
        SelfCareUser selfCareUser = SelfCareUser.builder(userId.toString()).email("test@example.com").build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(selfCareUser, "password", authorities);
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
        UserGroupService groupServiceMock = Mockito.mock(UserGroupService.class);
        UserGroupInfo groupInfo = TestUtils.mockInstance(new UserGroupInfo());
        UserInfo user = TestUtils.mockInstance(new UserInfo());
        user.setId(userId.toString());
        groupInfo.setMembers(List.of(user));
        Mockito.when(groupServiceMock.getUserGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(groupInfo));

        ProductsConnector productsConnectorMock = Mockito.mock(ProductsConnector.class);
        Product product = TestUtils.mockInstance(new Product());
        ProductRoleInfo productRoleInfo = TestUtils.mockInstance(new ProductRoleInfo());
        ProductRoleInfo.ProductRole productRole1 = TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1, "setCode");
        productRole1.setCode(productRole);
        productRoleInfo.setRoles(List.of(productRole1));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<PartyRole, ProductRoleInfo>(PartyRole.class);
        roleMappings.put(PartyRole.OPERATOR, productRoleInfo);
        product.setRoleMappings(roleMappings);

        Mockito.when(productsConnectorMock.getProduct(Mockito.anyString()))
                .thenReturn(product);


        File file = ResourceUtils.getFile(privateKey.getResourceLocation());
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        String kid = "kid";
        environmentVariables.set("JWT_TOKEN_EXCHANGE_ISSUER", "https://dev.selfcare.pagopa.it");
        String issuer = "https://dev.selfcare.pagopa.it";
        ExchangeTokenService exchangeTokenService = new ExchangeTokenService(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, jwtSigningKey, "PT5S", kid, issuer);
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
        assertEquals(issuer, exchangedClaims.getIssuer());
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
        assertEquals(1, institution.getRoles().size());
        List<String> groups = (List<String>) exchangedClaims.get("groups");
        assertEquals(groupInfo.getId(), groups.get(0));
        assertEquals(institutionInfo.getTaxCode(), institution.getTaxCode());
        Mockito.verify(jwtServiceMock, Mockito.times(1))
                .getClaims(Mockito.any());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitution(institutionId);
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .getUserGroups(Optional.of(institutionId), Optional.of(productId), Optional.of(userId), Pageable.unpaged());
        Mockito.verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock, groupServiceMock);
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
            LinkedHashMap<String, Object> o = (LinkedHashMap) get(INSTITUTION);
            ExchangeTokenService.Institution institution = new ExchangeTokenService.Institution();
            institution.setId(o.get("id").toString());
            institution.setRoles((List<ExchangeTokenService.Role>) o.get("roles"));
            institution.setTaxCode(o.get("fiscal_code").toString());
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