package it.pagopa.selfcare.dashboard.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.core.UserGroupV2Service;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.config.ExchangeTokenProperties;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.BackOfficeConfigurations;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRole;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.support.PageableExecutionUtils.getPage;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
class ExchangeTokenServiceV2Test {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    private static final String COLLAUDO_ENV = "Collaudo";


    @BeforeEach
    void cleanContext() {
        TestSecurityContextHolder.clearContext();
        environmentVariables.set("TOKEN_EXCHANGE_BILLING_URL", "http://localhost:8080/#selfcareToken=<IdentityToken>");
        environmentVariables.set("TOKEN_EXCHANGE_BILLING_AUDIENCE", "test");
    }


    @Test
    void exchange_illegalBase64Signature() {
        // given
        String jwtSigningKey = "invalid signature";
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        // when
        Executable executable = () -> new ExchangeTokenServiceV2(null, null, null, null, properties, null, null, new InstitutionResourceMapperImpl());
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
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        // when
        Executable executable = () -> new ExchangeTokenServiceV2(null, null, null, null, properties, null, null, new InstitutionResourceMapperImpl());
        // then
        assertThrows(InvalidKeySpecException.class, executable);
    }


    @Test
    void exchange_cannotParsePKCS1Key() {
        // given
        String jwtSigningKey = "-----BEGIN RSA PRIVATE KEY-----"
                + Base64.getEncoder().encodeToString("invalid signature".getBytes())
                + "-----END RSA PRIVATE KEY-----";
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        // when
        Executable executable = () -> new ExchangeTokenServiceV2(null, null, null, null, properties, null, null, new InstitutionResourceMapperImpl());
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertTrue(e.getMessage().startsWith("failed to construct sequence from byte[]"));
    }


    @Test
    void exchange_noAuth() throws Exception {
        // given
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setDuration("PT5S");
        JwtService jwtServiceMock = mock(JwtService.class);
        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, null, null, null, properties, null, null, new InstitutionResourceMapperImpl());
        // when
        Executable executable = () -> ExchangeTokenServiceV2.exchange(null, null, Optional.empty());
        // then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals("Authentication is required", e.getMessage());
        verifyNoInteractions(jwtServiceMock);
    }

    @Test
    void exchange_SelfCareAuthOnDifferentInstId() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        JwtService jwtServiceMock = mock(JwtService.class);
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setDuration("PT5S");

        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setRole(MANAGER);
        onboardedProduct.setProductId(productId);
        onboardedProduct.setProductRole("productRole");

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(Collections.emptyList());

        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, "userId")).thenReturn(userInstitution);

        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, null, null, null, properties, null, userApiConnector, new InstitutionResourceMapperImpl());
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority("differentInstitutionId", roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(SelfCareUser.builder("userId").build(), "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        // when
        Executable executable = () -> ExchangeTokenServiceV2.exchange(institutionId, productId, Optional.empty());
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Product Granted SelfCareAuthority is required for product '" + productId + "' and institution '" + institutionId + "'", e.getMessage());
        verifyNoInteractions(jwtServiceMock);
    }


    @Test
    void exchange_SelfCareAuthOnDifferentProductId() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        JwtService jwtServiceMock = mock(JwtService.class);
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setDuration("PT5S");

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(Collections.emptyList());

        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, "userId")).thenReturn(userInstitution);

        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, null, null, null, properties, null, userApiConnector, new InstitutionResourceMapperImpl());
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", "differentProductId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(SelfCareUser.builder("userId").build(), "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        // when
        Executable executable = () -> ExchangeTokenServiceV2.exchange(institutionId, productId, Optional.empty());
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Product Granted SelfCareAuthority is required for product '" + productId + "' and institution '" + institutionId + "'", e.getMessage());
        verifyNoInteractions(jwtServiceMock);
    }


    @Test
    void exchange_noSessionTokenClaims() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String credential = "password";
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setDuration("PT5S");
        JwtService jwtServiceMock = mock(JwtService.class);
        when(jwtServiceMock.getClaims(credential))
                .thenReturn(null);
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        UUID userId = UUID.randomUUID();
        SelfCareUser selfCareUser = SelfCareUser.builder(userId.toString()).email("test@example.com").build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(selfCareUser, "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);

        InstitutionService institutionServiceMock = mock(InstitutionService.class);
        Institution institutionInfo = new Institution();
        when(institutionServiceMock.getInstitutionById(institutionId))
                .thenReturn(institutionInfo);
        final Pageable pageable = Pageable.ofSize(100);
        UserGroupV2Service groupServiceMock = mock(UserGroupV2Service.class);
        UserGroupInfo groupInfo = new UserGroupInfo();
        UserInfo user = new UserInfo();
        user.setId(userId.toString());
        groupInfo.setMembers(List.of(user));
        final List<UserGroupInfo> groupInfos = new ArrayList<>(pageable.getPageSize());
        for (int i = 0; i < pageable.getPageSize(); i++) {
            groupInfos.add(groupInfo);
        }
        when(groupServiceMock.getUserGroups(institutionId, productId, userId, pageable))
                .thenAnswer(invocation -> getPage(groupInfos, invocation.getArgument(3, Pageable.class), () -> pageable.getPageSize() + 1));


        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setRole(MANAGER);
        onboardedProduct.setProductId(productId);
        onboardedProduct.setProductRole("productRole");
        onboardedProduct.setStatus(RelationshipState.ACTIVE);

        OnboardedProduct onboardedProduct2 = new OnboardedProduct();
        onboardedProduct2.setRole(MANAGER);
        onboardedProduct2.setProductId(productId);
        onboardedProduct2.setProductRole("productRole");
        onboardedProduct2.setStatus(RelationshipState.DELETED);


        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(List.of(onboardedProduct, onboardedProduct2));

        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, String.valueOf(userId))).thenReturn(userInstitution);

        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, institutionServiceMock, groupServiceMock, null, properties, null, userApiConnector, new InstitutionResourceMapperImpl());

        // when
        Executable executable = () -> ExchangeTokenServiceV2.exchange(institutionId, productId, Optional.empty());
        // then
        RuntimeException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Session token claims is required", e.getMessage());
        verify(jwtServiceMock, times(1))
                .getClaims(credential);
        verifyNoMoreInteractions(jwtServiceMock);
    }


    @Test
    void exchange_noInstitutionInfo() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setDuration("PT5S");
        JwtService jwtServiceMock = mock(JwtService.class);
        InstitutionService institutionServiceMock = mock(InstitutionService.class);
        ProductsConnector productsConnectorMock = mock(ProductsConnector.class);
        UserGroupV2Service groupServiceMock = mock(UserGroupV2Service.class);

        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setRole(MANAGER);
        onboardedProduct.setProductId(productId);
        onboardedProduct.setProductRole("productRole");
        onboardedProduct.setStatus(RelationshipState.ACTIVE);

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(List.of(onboardedProduct));

        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, "userId")).thenReturn(userInstitution);

        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, properties, null, userApiConnector, new InstitutionResourceMapperImpl());
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(SelfCareUser.builder("userId").build(), "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        // when
        Executable executable = () -> ExchangeTokenServiceV2.exchange(institutionId, productId, Optional.empty());
        // then
        RuntimeException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Institution info is required", e.getMessage());
        verify(institutionServiceMock, times(1))
                .getInstitutionById(institutionId);
        verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock);
    }

    @ParameterizedTest
    @EnumSource(PrivateKey.class)
    void exchange_nullGroupInfo(PrivateKey privateKey) throws Exception {
        // given
        String realm = "identityTokenAudienceFromProduct";
        String jti = "id";
        String sub = "subject";
        Date iat = Date.from(Instant.now().minusSeconds(1));
        Date exp = Date.from(iat.toInstant().plusSeconds(5));
        String institutionId = "institutionId";
        String productId = "productId";
        String productRole = "productRole";
        String credential = "password";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, productRole, productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        UUID userId = UUID.randomUUID();
        SelfCareUser selfCareUser = SelfCareUser.builder(userId.toString()).email("test@example.com").build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(selfCareUser, "password", authorities);

        ProductsConnector productsConnectorMock = mock(ProductsConnector.class);
        Product product = new Product();
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo.setRoles(List.of(productRole1));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class);
        roleMappings.put(PartyRole.OPERATOR, productRoleInfo);
        product.setRoleMappings(roleMappings);
        product.setIdentityTokenAudience(realm);
        product.setUrlBO("http://localhost:8080/#selfcareToken=<IdentityToken>");
        when(productsConnectorMock.getProduct(productId))
                .thenReturn(product);

        TestSecurityContextHolder.setAuthentication(authentication);
        JwtService jwtServiceMock = mock(JwtService.class);
        when(jwtServiceMock.getClaims(credential))
                .thenReturn(Jwts.claims()
                        .setId(jti)
                        .setSubject(sub)
                        .setIssuedAt(iat)
                        .setExpiration(exp));
        InstitutionService institutionServiceMock = mock(InstitutionService.class);
        Institution institutionInfo = new Institution();
        institutionInfo.setId(institutionId);
        institutionInfo.setDescription("description");
        institutionInfo.setTaxCode("taxCode");
        institutionInfo.setSubunitCode("subunitCode");
        institutionInfo.setSubunitType("subunitType");
        institutionInfo.setAooParentCode("AOO");
        institutionInfo.setOriginId("id");
        when(institutionServiceMock.getInstitutionById(institutionId))
                .thenReturn(institutionInfo);
        UserGroupV2Service groupServiceMock = mock(UserGroupV2Service.class);
        when(groupServiceMock.getUserGroups(institutionId, productId, userId, Pageable.ofSize(100)))
                .thenAnswer(invocation -> getPage(emptyList(), invocation.getArgument(3, Pageable.class), () -> 0L));
        File file = ResourceUtils.getFile(privateKey.getResourceLocation());
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        String kid = "kid";
        properties.setDuration("PT5S");
        properties.setKid(kid);
        environmentVariables.set("JWT_TOKEN_EXCHANGE_ISSUER", "https://dev.selfcare.pagopa.it");
        String issuer = "https://dev.selfcare.pagopa.it";
        properties.setIssuer(issuer);
        UserV2Service UserV2Service = mock(UserV2Service.class);
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        Map<String, WorkContact> workContactMap = new HashMap<>();
        WorkContact contact = new WorkContact();
        CertifiedField<String> email = new CertifiedField<>();
        email.setValue("email");
        contact.setEmail(email);
        workContactMap.put(institutionId, contact);
        user.setWorkContacts(workContactMap);
        when(UserV2Service.getUserById(String.valueOf(userId), null, null)).thenReturn(user);

        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setRole(MANAGER);
        onboardedProduct.setProductId(productId);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        onboardedProduct.setProductRole("productRole");

        OnboardedProduct onboardedProduct2 = new OnboardedProduct();
        onboardedProduct2.setRole(MANAGER);
        onboardedProduct2.setProductId(productId);
        onboardedProduct2.setStatus(RelationshipState.ACTIVE);
        onboardedProduct2.setProductRole("productRole2");

        OnboardedProduct onboardedProduct3 = new OnboardedProduct();
        onboardedProduct3.setRole(MANAGER);
        onboardedProduct3.setProductId(productId);
        onboardedProduct3.setStatus(RelationshipState.DELETED);
        onboardedProduct3.setProductRole("productRole3");

        OnboardedProduct onboardedProduct4 = new OnboardedProduct();
        onboardedProduct4.setRole(MANAGER);
        onboardedProduct4.setProductId(productId);
        onboardedProduct4.setStatus(RelationshipState.DELETED);
        onboardedProduct4.setProductRole("productRole4");

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(List.of(onboardedProduct, onboardedProduct2));

        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, String.valueOf(userId))).thenReturn(userInstitution);

        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, properties, UserV2Service, userApiConnector, new InstitutionResourceMapperImpl());
        // when
        final ExchangedToken exchangedToken = ExchangeTokenServiceV2.exchange(institutionId, productId, Optional.empty());
        // then
        assertEquals(product.getUrlBO(), exchangedToken.getBackOfficeUrl());
        assertNotNull(exchangedToken.getIdentityToken());
        Jws<Claims> claimsJws = Jwts.parser()
                .setSigningKey(loadPublicKey())
                .parseClaimsJws(exchangedToken.getIdentityToken());
        assertNotNull(claimsJws);
        assertNotNull(claimsJws.getHeader());
        assertEquals(kid, claimsJws.getHeader().getKeyId());
        TestTokenExchangeClaims exchangedClaims = new TestTokenExchangeClaims(claimsJws.getBody());
        assertNotEquals(jti, exchangedClaims.getId());
        assertNotEquals(0, exp.compareTo(exchangedClaims.getExpiration()));
        assertEquals(userId.toString(), exchangedClaims.getSubject());
        assertEquals(issuer, exchangedClaims.getIssuer());
        assertEquals(realm, exchangedClaims.getAudience());
        // https://github.com/jwtk/jjwt/issues/122:
        // The JWT RFC *mandates* NumericDate values are represented as seconds.
        // Because java.util.Date requires milliseconds, we need to multiply by 1000:
        assertEquals(exp.toInstant().getEpochSecond(), exchangedClaims.getDesiredExpiration().toInstant().getEpochSecond());
        assertTrue(exchangedClaims.getIssuedAt().after(iat));
        assertTrue(exchangedClaims.getExpiration().after(exp));
        assertTrue(exchangedClaims.getExpiration().after(exchangedClaims.getIssuedAt()));
        ExchangeTokenServiceV2.Institution institution = exchangedClaims.getInstitution();
        assertNotNull(institution);
        assertNull(institution.getGroups());
        assertEquals(institutionId, institution.getId());
        assertEquals(institutionInfo.getTaxCode(), institution.getTaxCode());
        assertNotNull(institution.getRoles());
        assertEquals(2, institution.getRoles().size());
        assertFalse(exchangedClaims.containsKey("groups"));
        verify(jwtServiceMock, times(1))
                .getClaims(credential);
        verify(institutionServiceMock, times(1))
                .getInstitutionById(institutionId);
        verify(groupServiceMock, times(1))
                .getUserGroups(institutionId, productId, userId, Pageable.ofSize(100));
        verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock, groupServiceMock);
    }


    @ParameterizedTest
    @EnumSource(PrivateKey.class)
    void exchange_ok(PrivateKey privateKey) throws Exception {
        // given
        String realm = "identityTokenAudienceFromProduct";
        String jti = "id";
        String sub = "subject";
        String credential = "password";
        Date iat = Date.from(Instant.now().minusSeconds(1));
        Date exp = Date.from(iat.toInstant().plusSeconds(5));
        String institutionId = "institutionId";
        String productId = "productId";
        String productRole = "productRole";
        final Pageable pageable = Pageable.ofSize(100);
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, productRole, productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        UUID userId = UUID.randomUUID();
        SelfCareUser selfCareUser = SelfCareUser.builder(userId.toString()).email("test@example.com").build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(selfCareUser, "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        JwtService jwtServiceMock = mock(JwtService.class);
        when(jwtServiceMock.getClaims(credential))
                .thenReturn(Jwts.claims()
                        .setId(jti)
                        .setSubject(sub)
                        .setIssuedAt(iat)
                        .setExpiration(exp));
        InstitutionService institutionServiceMock = mock(InstitutionService.class);
        UserV2Service UserV2Service = mock(UserV2Service.class);
        Institution institutionInfo = new Institution();
        institutionInfo.setId(institutionId);
        institutionInfo.setDescription("description");
        institutionInfo.setTaxCode("taxCode");
        institutionInfo.setSubunitCode("subunitCode");
        institutionInfo.setSubunitType("subunitType");
        institutionInfo.setAooParentCode("AOO");
        institutionInfo.setOriginId("id");
        when(institutionServiceMock.getInstitutionById(institutionId))
                .thenReturn(institutionInfo);
        UserGroupV2Service groupServiceMock = mock(UserGroupV2Service.class);
        UserGroupInfo groupInfo = new UserGroupInfo();
        UserInfo user = new UserInfo();
        user.setId(userId.toString());
        groupInfo.setMembers(List.of(user));
        groupInfo.setId("id");
        final List<UserGroupInfo> groupInfos = new ArrayList<>(pageable.getPageSize());
        for (int i = 0; i < pageable.getPageSize(); i++) {
            groupInfos.add(groupInfo);
        }
        when(groupServiceMock.getUserGroups(institutionId, productId, userId, pageable))
                .thenAnswer(invocation -> getPage(groupInfos, invocation.getArgument(3, Pageable.class), () -> pageable.getPageSize() + 1));
        ProductsConnector productsConnectorMock = mock(ProductsConnector.class);
        Product product = new Product();
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo.setRoles(List.of(productRole1));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class);
        roleMappings.put(PartyRole.OPERATOR, productRoleInfo);
        product.setRoleMappings(roleMappings);
        product.setIdentityTokenAudience(realm);
        Map<String, BackOfficeConfigurations> backOfficeConfigurationsMap = new HashMap<>();
        BackOfficeConfigurations backOfficeConfigurations = new BackOfficeConfigurations();
        backOfficeConfigurations.setUrl("http://localhost:8080/#selfcareToken=<IdentityToken>");
        backOfficeConfigurations.setIdentityTokenAudience("identityTokenAudienceFromProduct");
        backOfficeConfigurationsMap.put(COLLAUDO_ENV, backOfficeConfigurations);
        product.setBackOfficeEnvironmentConfigurations(backOfficeConfigurationsMap);
        when(productsConnectorMock.getProduct(productId))
                .thenReturn(product);
        File file = ResourceUtils.getFile(privateKey.getResourceLocation());
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        String kid = "kid";
        environmentVariables.set("JWT_TOKEN_EXCHANGE_ISSUER", "https://dev.selfcare.pagopa.it");
        String issuer = "https://dev.selfcare.pagopa.it";
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setKid(kid);
        properties.setDuration("PT5S");
        properties.setIssuer(issuer);
        User pdvUser = new User();
        pdvUser.setId(UUID.randomUUID().toString());
        Map<String, WorkContact> workContactMap = new HashMap<>();
        WorkContact contact = new WorkContact();
        CertifiedField<String> email = new CertifiedField<>();
        email.setValue("email");
        contact.setEmail(email);
        workContactMap.put(institutionId, contact);
        pdvUser.setWorkContacts(workContactMap);
        when(UserV2Service.getUserById(String.valueOf(userId), null, null)).thenReturn(pdvUser);

        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setRole(MANAGER);
        onboardedProduct.setProductId(productId);
        onboardedProduct.setProductRole(productRole);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);

        OnboardedProduct onboardedProduct2 = new OnboardedProduct();
        onboardedProduct2.setRole(MANAGER);
        onboardedProduct2.setProductId(productId);
        onboardedProduct2.setProductRole(productRole + "2");
        onboardedProduct2.setStatus(RelationshipState.DELETED);


        OnboardedProduct onboardedProduct3 = new OnboardedProduct();
        onboardedProduct3.setRole(MANAGER);
        onboardedProduct3.setProductId(productId);
        onboardedProduct3.setProductRole("admin2");

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(List.of(onboardedProduct, onboardedProduct2));
        Institution institutionMock = new Institution();
        institutionMock.setId(institutionId);
        institutionMock.setDescription("description");
        institutionMock.setTaxCode("taxCode");
        institutionMock.setSubunitCode("subunitCode");
        institutionMock.setSubunitType("subunitType");
        institutionMock.setAooParentCode("AOO");
        institutionMock.setOriginId("id");
        when(institutionServiceMock.getInstitutionById(institutionId)).thenReturn(institutionMock);
        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, String.valueOf(userId))).thenReturn(userInstitution);
        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, properties, UserV2Service, userApiConnector, new InstitutionResourceMapperImpl());
        // when
        final ExchangedToken exchangedToken = ExchangeTokenServiceV2.exchange(institutionId, productId, Optional.of(COLLAUDO_ENV));
        // then
        assertEquals(product.getBackOfficeEnvironmentConfigurations().get(COLLAUDO_ENV).getUrl(),
                exchangedToken.getBackOfficeUrl());
        Jws<Claims> claimsJws = Jwts.parser()
                .setSigningKey(loadPublicKey())
                .parseClaimsJws(exchangedToken.getIdentityToken());
        assertNotNull(claimsJws);
        assertNotNull(claimsJws.getHeader());
        assertEquals(kid, claimsJws.getHeader().getKeyId());
        TestTokenExchangeClaims exchangedClaims = new TestTokenExchangeClaims(claimsJws.getBody());
        assertNotEquals(jti, exchangedClaims.getId());
        assertNotEquals(0, exp.compareTo(exchangedClaims.getExpiration()));
        assertEquals(userId.toString(), exchangedClaims.getSubject());
        assertEquals(issuer, exchangedClaims.getIssuer());
        assertEquals(realm, exchangedClaims.getAudience());
        assertEquals(exp.toInstant().getEpochSecond(), exchangedClaims.getDesiredExpiration().toInstant().getEpochSecond());
        assertTrue(exchangedClaims.getIssuedAt().after(iat));
        assertTrue(exchangedClaims.getExpiration().after(exp));
        assertTrue(exchangedClaims.getExpiration().after(exchangedClaims.getIssuedAt()));
        ExchangeTokenServiceV2.Institution institution = exchangedClaims.getInstitution();
        assertNotNull(institution);
        assertEquals(institutionInfo.getDescription(), institution.getName());
        assertEquals(institutionId, institution.getId());
        checkNotNullFields(institution, "parentDescription", "rootParent");
        assertEquals(1, institution.getRoles().size());
        List<String> groups = institution.getGroups();
        assertEquals(pageable.getPageSize(), groups.size());
        assertTrue(groups.stream().allMatch(groupId -> groupId.equals(groupInfo.getId())));
        assertEquals(institutionInfo.getTaxCode(), institution.getTaxCode());
        verify(jwtServiceMock, times(1))
                .getClaims(credential);
        verify(institutionServiceMock, times(1))
                .getInstitutionById(institutionId);
        verify(groupServiceMock, times(1))
                .getUserGroups(institutionId, productId, userId, Pageable.ofSize(100));
        verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock, groupServiceMock);
    }

    @Test
    void billingExchange_noAuth() throws Exception {
        // given
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setDuration("PT5S");
        JwtService jwtServiceMock = mock(JwtService.class);
        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, null, null, null, properties, null, null, new InstitutionResourceMapperImpl());
        // when
        Executable executable = () -> ExchangeTokenServiceV2.retrieveBillingExchangedToken(null, null);
        // then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals("Authentication is required", e.getMessage());
        verifyNoInteractions(jwtServiceMock);
    }

    @Test
    void billingExchange_noSessionTokenClaims() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String credential = "password";
        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setDuration("PT5S");
        JwtService jwtServiceMock = mock(JwtService.class);
        when(jwtServiceMock.getClaims(credential))
                .thenReturn(null);
        ProductsConnector productsConnector = mock(ProductsConnector.class);
        ProductTree productTree = new ProductTree();
        Product product = new Product();
        product.setId("prod-io");
        productTree.setNode(product);
        when(productsConnector.getProductsTree()).thenReturn(List.of(productTree));
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        UUID userId = UUID.randomUUID();
        SelfCareUser selfCareUser = SelfCareUser.builder(userId.toString()).email("test@example.com").build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(selfCareUser, "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        Institution institutionInfo = new Institution();
        UserGroupV2Service groupServiceMock = mock(UserGroupV2Service.class);
        InstitutionService institutionServiceMock = mock(InstitutionService.class);

        UserGroupInfo groupInfo = new UserGroupInfo();
        UserInfo user = new UserInfo();
        user.setId(userId.toString());
        groupInfo.setMembers(List.of(user));
        final Pageable pageable = Pageable.ofSize(100);
        final List<UserGroupInfo> groupInfos = new ArrayList<>(pageable.getPageSize());
        for (int i = 0; i < pageable.getPageSize(); i++) {
            groupInfos.add(groupInfo);
        }
        when(groupServiceMock.getUserGroups(institutionId, null, userId, Pageable.ofSize(100)))
                .thenAnswer(invocation -> getPage(groupInfos, invocation.getArgument(3, Pageable.class), () -> pageable.getPageSize() + 1));

        when(institutionServiceMock.getInstitutionById(institutionId))
                .thenReturn(institutionInfo);

        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setRole(MANAGER);
        onboardedProduct.setProductId(productId);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        onboardedProduct.setProductRole("productRole");

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(List.of(onboardedProduct));

        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, String.valueOf(userId))).thenReturn(userInstitution);

        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnector, properties, null, userApiConnector, new InstitutionResourceMapperImpl());

        Executable executable = () -> ExchangeTokenServiceV2.retrieveBillingExchangedToken(institutionId, null);
        // then
        RuntimeException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Session token claims is required", e.getMessage());
        verify(jwtServiceMock, times(1))
                .getClaims(credential);
        verifyNoMoreInteractions(jwtServiceMock);
    }


    @Test
    void billingExchange_noInstitutionInfo() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";

        File file = ResourceUtils.getFile("classpath:certs/PKCS8key.pem");
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setDuration("PT5S");
        JwtService jwtServiceMock = mock(JwtService.class);
        InstitutionService institutionServiceMock = mock(InstitutionService.class);
        ProductsConnector productsConnectorMock = mock(ProductsConnector.class);
        UserGroupV2Service groupServiceMock = mock(UserGroupV2Service.class);
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setRole(MANAGER);
        onboardedProduct.setProductId(productId);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        onboardedProduct.setProductRole("productRole");

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(List.of(onboardedProduct));

        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, "ccbc5350-0ba7-47bc-9f61-8c65001939f9")).thenReturn(userInstitution);

        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, properties, null, userApiConnector, new InstitutionResourceMapperImpl());
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(SelfCareUser.builder("ccbc5350-0ba7-47bc-9f61-8c65001939f9").build(), "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        // when
        Executable executable = () -> ExchangeTokenServiceV2.retrieveBillingExchangedToken(institutionId, null);
        // then
        RuntimeException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Institution info is required", e.getMessage());
        verify(institutionServiceMock, times(1))
                .getInstitutionById(institutionId);
        verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock);
    }

    @ParameterizedTest
    @EnumSource(PrivateKey.class)
    void billingExchange_nullGroupInfo(PrivateKey privateKey) throws Exception {
        // given
        String jti = "id";
        String sub = "subject";
        String credential = "password";
        Date iat = Date.from(Instant.now().minusSeconds(1));
        Date exp = Date.from(iat.toInstant().plusSeconds(5));
        String institutionId = "institutionId";
        String productRole = "productRole";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, productRole, "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        UUID userId = UUID.randomUUID();
        SelfCareUser selfCareUser = SelfCareUser.builder(userId.toString()).email("test@example.com").build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(selfCareUser, "password", authorities);

        ProductsConnector productsConnectorMock = mock(ProductsConnector.class);
        Product product = new Product();
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo.setRoles(List.of(productRole1));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class);
        roleMappings.put(PartyRole.OPERATOR, productRoleInfo);
        product.setRoleMappings(roleMappings);
        ProductTree productTree = new ProductTree();
        productTree.setNode(product);
        when(productsConnectorMock.getProductsTree()).thenReturn(List.of(productTree));

        TestSecurityContextHolder.setAuthentication(authentication);
        JwtService jwtServiceMock = mock(JwtService.class);
        when(jwtServiceMock.getClaims(credential))
                .thenReturn(Jwts.claims()
                        .setId(jti)
                        .setSubject(sub)
                        .setIssuedAt(iat)
                        .setExpiration(exp));
        InstitutionService institutionServiceMock = mock(InstitutionService.class);
        Institution institutionInfo = new Institution();
        institutionInfo.setId(institutionId);
        institutionInfo.setDescription("description");
        institutionInfo.setTaxCode("taxCode");
        institutionInfo.setSubunitCode("subunitCode");
        institutionInfo.setSubunitType("subunitType");
        institutionInfo.setAooParentCode("AOO");
        institutionInfo.setOriginId("id");
        when(institutionServiceMock.getInstitutionById(institutionId))
                .thenReturn(institutionInfo);
        UserGroupV2Service groupServiceMock = mock(UserGroupV2Service.class);
        when(groupServiceMock.getUserGroups(institutionId, null, userId, Pageable.ofSize(100)))
                .thenAnswer(invocation -> getPage(emptyList(), invocation.getArgument(3, Pageable.class), () -> 0L));
        File file = ResourceUtils.getFile(privateKey.getResourceLocation());
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        String kid = "kid";
        properties.setDuration("PT5S");
        properties.setKid(kid);
        environmentVariables.set("JWT_TOKEN_EXCHANGE_ISSUER", "https://dev.selfcare.pagopa.it");
        environmentVariables.set("TOKEN_EXCHANGE_BILLING_URL", "http://localhost:8080/#selfcareToken=<IdentityToken>");
        environmentVariables.set("TOKEN_EXCHANGE_BILLING_AUDIENCE", "test");
        String issuer = "https://dev.selfcare.pagopa.it";
        properties.setIssuer(issuer);
        properties.setBillingUrl("http://localhost:8080/#selfcareToken=<IdentityToken>");
        UserV2Service UserV2Service = mock(UserV2Service.class);
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        Map<String, WorkContact> workContactMap = new HashMap<>();
        WorkContact contact = new WorkContact();
        CertifiedField<String> email = new CertifiedField<>();
        email.setValue("email");
        contact.setEmail(email);
        workContactMap.put(institutionId, contact);
        user.setWorkContacts(workContactMap);
        when(UserV2Service.getUserById(String.valueOf(userId), null, null)).thenReturn(user);
        UserInstitution userInstitution = new UserInstitution();
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setRole(MANAGER);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        onboardedProduct.setProductId("productId");
        onboardedProduct.setProductRole("admin");
        userInstitution.setProducts(List.of(onboardedProduct));

        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, String.valueOf(userId))).thenReturn(userInstitution);
        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, properties, UserV2Service, userApiConnector, new InstitutionResourceMapperImpl());
        // when
        final ExchangedToken exchangedToken = ExchangeTokenServiceV2.retrieveBillingExchangedToken(institutionId, null);
        // then
        assertNotNull(exchangedToken.getIdentityToken());
        Jws<Claims> claimsJws = Jwts.parser()
                .setSigningKey(loadPublicKey())
                .parseClaimsJws(exchangedToken.getIdentityToken());
        assertNotNull(claimsJws);
        assertNotNull(claimsJws.getHeader());
        assertEquals(kid, claimsJws.getHeader().getKeyId());
        TestTokenExchangeClaims exchangedClaims = new TestTokenExchangeClaims(claimsJws.getBody());
        assertNotEquals(jti, exchangedClaims.getId());
        assertNotEquals(0, exp.compareTo(exchangedClaims.getExpiration()));
        assertEquals(userId.toString(), exchangedClaims.getSubject());
        assertEquals(issuer, exchangedClaims.getIssuer());
        // https://github.com/jwtk/jjwt/issues/122:
        // The JWT RFC *mandates* NumericDate values are represented as seconds.
        // Because java.util.Date requires milliseconds, we need to multiply by 1000:
        assertEquals(exp.toInstant().getEpochSecond(), exchangedClaims.getDesiredExpiration().toInstant().getEpochSecond());
        assertTrue(exchangedClaims.getIssuedAt().after(iat));
        assertTrue(exchangedClaims.getExpiration().after(exp));
        assertTrue(exchangedClaims.getExpiration().after(exchangedClaims.getIssuedAt()));
        ExchangeTokenServiceV2.Institution institution = exchangedClaims.getInstitution();
        assertNotNull(institution);
        assertNull(institution.getGroups());
        assertEquals(institutionId, institution.getId());
        assertEquals(institutionInfo.getTaxCode(), institution.getTaxCode());
        assertNotNull(institution.getRoles());
        assertFalse(exchangedClaims.containsKey("groups"));
        verify(jwtServiceMock, times(1))
                .getClaims(credential);
        verify(institutionServiceMock, times(1))
                .getInstitutionById(institutionId);
        verify(groupServiceMock, times(1))
                .getUserGroups(institutionId, null, userId, Pageable.ofSize(100));
        verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock, groupServiceMock);
    }


    @ParameterizedTest
    @EnumSource(PrivateKey.class)
    void billingExchange_ok(PrivateKey privateKey) throws Exception {
        // given
        String lang = "en";
        String jti = "id";
        String credential = "password";
        Date iat = Date.from(Instant.now().minusSeconds(1));
        Date exp = Date.from(iat.toInstant().plusSeconds(5));
        String institutionId = "institutionId";
        String productRole = "productRole";
        final Pageable pageable = Pageable.ofSize(100);
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, productRole, "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        UUID userId = UUID.randomUUID();
        SelfCareUser selfCareUser = SelfCareUser.builder(userId.toString()).email("test@example.com").build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(selfCareUser, "password", authorities);
        TestSecurityContextHolder.setAuthentication(authentication);
        JwtService jwtServiceMock = mock(JwtService.class);
        when(jwtServiceMock.getClaims(credential))
                .thenReturn(Jwts.claims()
                        .setId(jti)
                        .setIssuedAt(iat)
                        .setExpiration(exp));
        InstitutionService institutionServiceMock = mock(InstitutionService.class);
        UserV2Service UserV2Service = mock(UserV2Service.class);
        Institution institutionInfo = new Institution();
        institutionInfo.setId(institutionId);
        institutionInfo.setDescription("description");
        institutionInfo.setTaxCode("taxCode");
        institutionInfo.setSubunitCode("subunitCode");
        institutionInfo.setSubunitType("subunitType");
        institutionInfo.setAooParentCode("AOO");
        institutionInfo.setOriginId("id");
        when(institutionServiceMock.getInstitutionById(institutionId))
                .thenReturn(institutionInfo);
        UserGroupV2Service groupServiceMock = mock(UserGroupV2Service.class);
        UserGroupInfo groupInfo = new UserGroupInfo();
        UserInfo user = new UserInfo();
        user.setId(userId.toString());
        groupInfo.setMembers(List.of(user));
        groupInfo.setId("id");
        final List<UserGroupInfo> groupInfos = new ArrayList<>(pageable.getPageSize());
        for (int i = 0; i < pageable.getPageSize(); i++) {
            groupInfos.add(groupInfo);
        }
        when(groupServiceMock.getUserGroups(institutionId, null, userId, pageable))
                .thenAnswer(invocation -> getPage(groupInfos, invocation.getArgument(3, Pageable.class), () -> pageable.getPageSize() + 1));
        ProductsConnector productsConnectorMock = mock(ProductsConnector.class);
        Product product = new Product();
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo.setRoles(List.of(productRole1));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class);
        roleMappings.put(PartyRole.OPERATOR, productRoleInfo);
        product.setRoleMappings(roleMappings);
        ProductTree productTree = new ProductTree();
        productTree.setNode(product);
        when(productsConnectorMock.getProductsTree()).thenReturn(List.of(productTree));
        File file = ResourceUtils.getFile(privateKey.getResourceLocation());
        String jwtSigningKey = Files.readString(file.toPath(), Charset.defaultCharset());
        String kid = "kid";
        environmentVariables.set("JWT_TOKEN_EXCHANGE_ISSUER", "https://dev.selfcare.pagopa.it");
        String issuer = "https://dev.selfcare.pagopa.it";
        ExchangeTokenProperties properties = new ExchangeTokenProperties();
        properties.setSigningKey(jwtSigningKey);
        properties.setKid(kid);
        properties.setDuration("PT5S");
        properties.setIssuer(issuer);
        properties.setBillingUrl("http://localhost:8080/#selfcareToken=<IdentityToken>");
        User pdvUser = new User();
        pdvUser.setId(UUID.randomUUID().toString());
        Map<String, WorkContact> workContactMap = new HashMap<>();
        WorkContact contact = new WorkContact();
        CertifiedField<String> email = new CertifiedField<>();
        email.setValue("email");
        contact.setEmail(email);
        workContactMap.put(institutionId, contact);
        pdvUser.setWorkContacts(workContactMap);
        when(UserV2Service.getUserById(String.valueOf(userId), null, null)).thenReturn(pdvUser);

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(Collections.emptyList());
        UserApiConnector userApiConnector = mock(UserApiConnector.class);
        when(userApiConnector.getProducts(institutionId, String.valueOf(userId))).thenReturn(userInstitution);
        ExchangeTokenServiceV2 ExchangeTokenServiceV2 = new ExchangeTokenServiceV2(jwtServiceMock, institutionServiceMock, groupServiceMock, productsConnectorMock, properties, UserV2Service, userApiConnector, new InstitutionResourceMapperImpl());
        // when
        final ExchangedToken exchangedToken = ExchangeTokenServiceV2.retrieveBillingExchangedToken(institutionId, lang);
        // then
        assertNotNull(exchangedToken.getIdentityToken());
        Jws<Claims> claimsJws = Jwts.parser()
                .setSigningKey(loadPublicKey())
                .parseClaimsJws(exchangedToken.getIdentityToken());
        assertNotNull(claimsJws);
        assertNotNull(claimsJws.getHeader());
        assertEquals(kid, claimsJws.getHeader().getKeyId());
        TestTokenExchangeClaims exchangedClaims = new TestTokenExchangeClaims(claimsJws.getBody());
        assertNotEquals(jti, exchangedClaims.getId());
        assertNotEquals(0, exp.compareTo(exchangedClaims.getExpiration()));
        assertEquals(userId.toString(), exchangedClaims.getSubject());
        assertEquals(issuer, exchangedClaims.getIssuer());
        // https://github.com/jwtk/jjwt/issues/122:
        // The JWT RFC *mandates* NumericDate values are represented as seconds.
        // Because java.util.Date requires milliseconds, we need to multiply by 1000:
        assertEquals(exp.toInstant().getEpochSecond(), exchangedClaims.getDesiredExpiration().toInstant().getEpochSecond());
        assertTrue(exchangedClaims.getIssuedAt().after(iat));
        assertTrue(exchangedClaims.getExpiration().after(exp));
        assertTrue(exchangedClaims.getExpiration().after(exchangedClaims.getIssuedAt()));
        ExchangeTokenServiceV2.Institution institution = exchangedClaims.getInstitution();
        assertNotNull(institution);
        assertEquals(institutionInfo.getDescription(), institution.getName());
        assertEquals(institutionId, institution.getId());
        checkNotNullFields(institution, "parentDescription", "rootParent");
        List<String> groups = institution.getGroups();
        assertEquals(pageable.getPageSize(), groups.size());
        assertTrue(groups.stream().allMatch(groupId -> groupId.equals(groupInfo.getId())));
        assertEquals(institutionInfo.getTaxCode(), institution.getTaxCode());
        verify(jwtServiceMock, times(1))
                .getClaims(credential);
        verify(institutionServiceMock, times(1))
                .getInstitutionById(institutionId);
        verify(groupServiceMock, times(1))
                .getUserGroups(institutionId, null, userId, Pageable.ofSize(100));
        verifyNoMoreInteractions(jwtServiceMock, institutionServiceMock, groupServiceMock);
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


    private static class TestTokenExchangeClaims extends ExchangeTokenServiceV2.TokenExchangeClaims {

        public TestTokenExchangeClaims(Map<String, Object> map) {
            super(map);
        }

        public Date getDesiredExpiration() {
            return getDate(DESIRED_EXPIRATION);
        }

        public ExchangeTokenServiceV2.Institution getInstitution() {
            LinkedHashMap<String, Object> organizationClaim = (LinkedHashMap) get(INSTITUTION);
            ExchangeTokenServiceV2.Institution institution = new ExchangeTokenServiceV2.Institution();
            institution.setId(organizationClaim.get("id").toString());
            institution.setRoles((List<ExchangeTokenServiceV2.Role>) organizationClaim.get("roles"));
            institution.setName(organizationClaim.get("name").toString());
            institution.setTaxCode(organizationClaim.get("fiscal_code").toString());
            institution.setGroups((List<String>) organizationClaim.get("groups"));
            institution.setSubUnitCode(organizationClaim.get("subUnitCode").toString());
            institution.setSubUnitType(organizationClaim.get("subUnitType").toString());
            institution.setAooParent(organizationClaim.get("aooParent").toString());
            institution.setOriginId(organizationClaim.get("ipaCode").toString());
            ExchangeTokenServiceV2.RootParent rootParent = (ExchangeTokenServiceV2.RootParent) organizationClaim.get("rootParent.description");
            institution.setRootParent(rootParent);
            return institution;
        }

    }


    @Getter
    private enum PrivateKey {
        PKCS1("classpath:certs/PKCS1Key.pem"),
        PKCS8("classpath:certs/PKCS8key.pem");

        private final String resourceLocation;

        PrivateKey(String resourceLocation) {
            this.resourceLocation = resourceLocation;
        }
    }

}
