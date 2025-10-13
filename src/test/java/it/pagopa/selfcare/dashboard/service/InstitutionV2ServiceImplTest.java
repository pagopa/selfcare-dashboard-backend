package it.pagopa.selfcare.dashboard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingsResponse;
import it.pagopa.selfcare.dashboard.client.CoreInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.client.OnboardingRestClient;
import it.pagopa.selfcare.dashboard.client.TokenRestClient;
import it.pagopa.selfcare.dashboard.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.model.institution.Institution;
import it.pagopa.selfcare.dashboard.model.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.dashboard.model.mapper.UserMapperImpl;
import it.pagopa.selfcare.dashboard.model.product.mapper.ProductMapper;
import it.pagopa.selfcare.dashboard.model.user.UserInfo;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGet;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGetResponse;
import it.pagopa.selfcare.product.service.ProductService;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDataResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionWithActions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.PENDING;
import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.TOBEVALIDATED;
import static it.pagopa.selfcare.onboarding.common.ProductId.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class InstitutionV2ServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private InstitutionV2ServiceImpl institutionV2Service;
    @Mock
    private UserApiRestClient userApiRestClient;
    @Mock
    private CoreInstitutionApiRestClient coreInstitutionApiRestClient;
    @Mock
    private OnboardingRestClient onboardingRestClient;
    @Mock
    private ProductService productService;
    @Mock
    private TokenRestClient tokenRestClient;
    @Spy
    private InstitutionMapperImpl institutionMapper;
    @Spy
    private ProductMapper productMapper;
    @Spy
    private UserMapperImpl userMapper;
    @Mock
    private UserV2ServiceImpl userV2Service;

    @BeforeEach
    void init() {
        super.setUp();
    }

    @Test
    void getInstitutionUserWithoutInstitutionId() {
        String userId = "userId";
        String loggedUserId = "loggedUserId";
        assertThrows(IllegalArgumentException.class, () -> institutionV2Service.getInstitutionUser(null, userId, loggedUserId));
    }

    @Test
    void getInstitutionUserWithoutUserId() {
        String institutionId = "institutionId";
        String loggedUserId = "loggedUserId";
        assertThrows(IllegalArgumentException.class, () -> institutionV2Service.getInstitutionUser(institutionId, null, loggedUserId));
    }

    @Test
    void getInstitutionUserNoUserFound() {
        String institutionId = "institutionId";
        String userId = "userId";
        String loggedUserId = "loggedUserId";

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);

        when(userV2Service.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> institutionV2Service.getInstitutionUser(institutionId, userId, loggedUserId));
    }

    @Test
    void getInstitutionUser() throws IOException {
        String institutionId = "institutionId";
        String userId = "userId";
        String loggedUserId = "loggedUserId";

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);

        ClassPathResource resource = new ClassPathResource("stubs/UserInfo.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<UserInfo> userInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        UserDataResponse userData = new UserDataResponse();
        userData.setUserId("123e4567-e89b-12d3-a456-426614174000");
        userData.setRole("MANAGER");

        when(userV2Service.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(userInfo);

        UserInfo actualUserInfo = institutionV2Service.getInstitutionUser("institutionId", "userId", "loggedUserId");
        assertEquals(userInfo.get(0).getId(), actualUserInfo.getId());
        Mockito.verify(userV2Service, Mockito.times(1)).getUsers(institutionId, userInfoFilter, loggedUserId);
    }

    @Test
    void findInstitutionById() throws IOException {
        String institutionId = "institutionId";
        String userId = "userId";

        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ClassPathResource userInstitutionResource = new ClassPathResource("expectations/UserInstitutionWithActions.json");
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        UserInstitutionWithActions userInstitutionWithActionsDto = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });


        ClassPathResource resource = new ClassPathResource("stubs/InstitutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiRestClient._getUserInstitutionWithPermission(institutionId, userId, null))
                .thenReturn(ResponseEntity.ok(userInstitutionWithActionsDto));
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId, null))
                .thenReturn(ResponseEntity.ok(institution));

        Institution result = institutionV2Service.findInstitutionById(institutionId);
        Assertions.assertNotNull(result.getOnboarding().get(0).getUserProductActions());
        Assertions.assertEquals(2, result.getOnboarding().get(0).getUserProductActions().size());
        Assertions.assertNotNull(result.getOnboarding().get(0).getCreatedAt());
        Mockito.verify(userApiRestClient, Mockito.times(1))
                ._getUserInstitutionWithPermission(institutionId, userId, null);
        Mockito.verify(coreInstitutionApiRestClient, Mockito.times(1))
                ._retrieveInstitutionByIdUsingGET(institutionId, null);
    }

    @Test
    void findInstitutionByIdWithoutInstitutionId() {
        assertThrows(IllegalArgumentException.class, () -> institutionV2Service.findInstitutionById(null));
    }

    @Test
    void findInstitutionByIdNoUserInstitution() {
        String institutionId = "institutionId";
        String userId = "userId";

        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        InstitutionResponse institutionResponse = Mockito.mock(InstitutionResponse.class);
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId, null)).thenReturn(ResponseEntity.ok(institutionResponse));
        when(userApiRestClient._getUserInstitutionWithPermission(institutionId, userId, null)).thenReturn(ResponseEntity.ok().build());
        assertThrows(AccessDeniedException.class, () -> institutionV2Service.findInstitutionById(institutionId));
        Mockito.verify(userApiRestClient, Mockito.times(1))._getUserInstitutionWithPermission(institutionId, userId, null);
    }

    @Test
    void findInstitutionByIdNoInstitution() throws IOException {
        String institutionId = "institutionId";
        String userId = "userId";

        ClassPathResource userInstitutionResource = new ClassPathResource("expectations/UserInstitutionWithActions.json");
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        UserInstitutionWithActions userInstitutionWithActionsDto = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });

        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);


        when(userApiRestClient._getUserInstitutionWithPermission(institutionId, userId, null)).thenReturn(ResponseEntity.ok(userInstitutionWithActionsDto));
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId, null)).thenReturn(ResponseEntity.ok(null));
        assertThrows(ResourceNotFoundException.class, () -> institutionV2Service.findInstitutionById(institutionId));
    }

    @Test
    void findInstitutionByIdNoOnboarding() throws IOException {
        String institutionId = "institutionId";
        String userId = "userId";

        ClassPathResource userInstitutionResource = new ClassPathResource("expectations/UserInstitutionWithActions.json");
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        UserInstitutionWithActions userInstitutionWithActionsDto = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });

        ClassPathResource resource = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        institution.setOnboarding(null);

        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userApiRestClient._getUserInstitutionWithPermission(institutionId, userId, null)).thenReturn(ResponseEntity.ok(userInstitutionWithActionsDto));
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId, null)).thenReturn(ResponseEntity.ok(institution));
        assertThrows(ResourceNotFoundException.class, () -> institutionV2Service.findInstitutionById(institutionId));
    }

    @Test
    void findInstitutionByIdLimited() throws IOException {
        String institutionId = "institutionId";
        String userId = "userId";

        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(OPERATOR, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ClassPathResource userInstitutionResource = new ClassPathResource("expectations/UserInstitutionWithActionsOperator.json");
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        UserInstitutionWithActions userInstitutionWithActionsDto = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });

        ClassPathResource resource = new ClassPathResource("stubs/InstitutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiRestClient._getUserInstitutionWithPermission(institutionId, userId, null)).thenReturn(ResponseEntity.ok(userInstitutionWithActionsDto));
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId, null)).thenReturn(ResponseEntity.ok(institution));

        Institution result = institutionV2Service.findInstitutionById(institutionId);
        Assertions.assertEquals("LIMITED", result.getOnboarding().get(0).getUserRole());
        Mockito.verify(userApiRestClient, Mockito.times(1))._getUserInstitutionWithPermission(institutionId, userId, null);
    }

    @Test
    void getPendingOnboarding_pendingFound() {
        String productId = "test-product";
        String status = PENDING.name();
        String subunitCode = "test-subunit";
        String taxCode = "test-taxCode";

        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(List.of(new OnboardingGet().productId(productId)));
        onboardingGetResponse.setCount((long) onboardingGetResponse.getItems().size());

        when(onboardingRestClient._getOnboardingWithFilter(null, null, null, null, productId, null,1, null, status, subunitCode, taxCode, null, null))
                .thenReturn(ResponseEntity.of(Optional.of(onboardingGetResponse)));

        Boolean actualResponse = institutionV2Service.verifyIfExistsPendingOnboarding(taxCode, subunitCode, productId);

        assertTrue(actualResponse);
        Mockito.verify(onboardingRestClient, Mockito.times(1))
                ._getOnboardingWithFilter(null, null, null, null, productId, null, 1, null, PENDING.name(), subunitCode, taxCode, null, null);
    }

    @Test
    void getOnboardingWithFilterOk() {
        String taxCode = "taxCode";
        String productId = "productId";
        String status = "status";

        OnboardingGet onboardingGet = new OnboardingGet();
        onboardingGet.setProductId(productId);

        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.addItemsItem(onboardingGet);

        doReturn(ResponseEntity.of(Optional.of(onboardingGetResponse)))
                .when(onboardingRestClient)
                ._getOnboardingWithFilter(null, null, null, null, productId, null,1, null, status, null, taxCode, null, null);

        boolean onboardingGetInfo = onboardingRestClient._getOnboardingWithFilter(null, null, null, null, productId, null, 1, null, status, null, taxCode, null, null).getBody() != null;
        Assertions.assertTrue(onboardingGetInfo);
    }

    @Test
    void getPendingOnboarding_pendingNotFound_tobeValidatedFound() {
        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(List.of(new OnboardingGet().productId("test-product")));
        onboardingGetResponse.setCount((long) onboardingGetResponse.getItems().size());

        when(onboardingRestClient._getOnboardingWithFilter(null, null, null, null, "test-product", null,1, null, PENDING.name(), null, "test-institution", null, null))
                .thenReturn(ResponseEntity.ok().build());
        when(onboardingRestClient._getOnboardingWithFilter(null, null, null, null, "test-product", null, 1, null, TOBEVALIDATED.name(), null, "test-institution", null, null))
                .thenReturn(ResponseEntity.of(Optional.of(onboardingGetResponse)));

        Boolean actualResponse = institutionV2Service.verifyIfExistsPendingOnboarding("test-institution", null, "test-product");

        assertTrue(actualResponse);
        Mockito.verify(onboardingRestClient, Mockito.times(1))
                ._getOnboardingWithFilter(null, null, null, null, "test-product", null, 1, null, PENDING.name(), null, "test-institution", null, null);
        Mockito.verify(onboardingRestClient, Mockito.times(1))
                ._getOnboardingWithFilter(null, null, null, null, "test-product", null, 1, null, TOBEVALIDATED.name(), null, "test-institution", null, null);
    }

    @Test
    void getPendingOnboarding_neitherFound() {
        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(List.of(new OnboardingGet().productId("test-product")));
        onboardingGetResponse.setCount((long) onboardingGetResponse.getItems().size());
        when(onboardingRestClient._getOnboardingWithFilter(null, null, null, null, "test-product", null, 1, null, PENDING.name(), null, "test-institution", null, null))
                .thenReturn(ResponseEntity.ok().build());
        when(onboardingRestClient._getOnboardingWithFilter(null, null, null, null, "test-product", null, 1, null, TOBEVALIDATED.name(), null, "test-institution", null, null))
                .thenReturn(ResponseEntity.ok().build());

        Boolean actualResponse = institutionV2Service.verifyIfExistsPendingOnboarding("test-institution", null, "test-product");

        assertFalse(actualResponse);
    }

    @Test
    void getOnboardingsInfo_WithoutFilter() {
        String institutionId = "institutionId";

        OnboardingResponse onboardingResponsePagopa = new OnboardingResponse();
        onboardingResponsePagopa.setProductId(PROD_PAGOPA.name());
        onboardingResponsePagopa.setStatus(OnboardingResponse.StatusEnum.ACTIVE);
        OnboardingResponse onboardingResponseIo = new OnboardingResponse();
        onboardingResponseIo.setProductId(PROD_IO.name());
        onboardingResponseIo.setStatus(OnboardingResponse.StatusEnum.ACTIVE);

        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        onboardingsResponse.setOnboardings(List.of(onboardingResponsePagopa, onboardingResponseIo));

        doReturn(ResponseEntity.of(Optional.of(onboardingsResponse)))
                .when(coreInstitutionApiRestClient)
                ._getOnboardingsInstitutionUsingGET(institutionId, null);

        OnboardingsResponse response = institutionV2Service.getOnboardingsInfoResponse(institutionId, null);
        Assertions.assertEquals(2, response.getOnboardings().size());
    }


    @Test
    void getOnboardingsInfo_WithFilter() {
        String institutionId = "institutionId";
        List<String> products = List.of(PROD_PAGOPA.name(), PROD_DASHBOARD_PSP.name());

        OnboardingResponse onboardingResponsePagopa = new OnboardingResponse();
        onboardingResponsePagopa.setProductId(PROD_PAGOPA.name());
        onboardingResponsePagopa.setStatus(OnboardingResponse.StatusEnum.ACTIVE);
        OnboardingResponse onboardingResponseDashboard = new OnboardingResponse();
        onboardingResponseDashboard.setProductId(PROD_DASHBOARD_PSP.name());
        onboardingResponseDashboard.setStatus(OnboardingResponse.StatusEnum.DELETED);
        OnboardingResponse onboardingResponseIo = new OnboardingResponse();
        onboardingResponseIo.setProductId(PROD_IO.name());
        onboardingResponseIo.setStatus(OnboardingResponse.StatusEnum.ACTIVE);

        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        onboardingsResponse.setOnboardings(List.of(onboardingResponsePagopa, onboardingResponseDashboard, onboardingResponseIo));

        doReturn(ResponseEntity.of(Optional.of(onboardingsResponse)))
                .when(coreInstitutionApiRestClient)
                ._getOnboardingsInstitutionUsingGET(institutionId, null);

        OnboardingsResponse response = institutionV2Service.getOnboardingsInfoResponse(institutionId, products);
        Assertions.assertEquals(1, response.getOnboardings().size());
    }

    @Test
    void getContract_shouldReturnContract_whenActiveOnboardingExists() {
        String institutionId = "inst1";
        String productId = "prod1";
        String tokenId = "token123";
        Resource expectedContract = new ByteArrayResource("contract".getBytes());

        OnboardingResponse onboarding = new OnboardingResponse();
        onboarding.setStatus(OnboardingResponse.StatusEnum.ACTIVE);
        onboarding.setCreatedAt(OffsetDateTime.now());
        onboarding.setTokenId(tokenId);

        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        onboardingsResponse.setOnboardings(List.of(onboarding));

        when(coreInstitutionApiRestClient._getOnboardingsInstitutionUsingGET(institutionId, productId))
                .thenReturn(ResponseEntity.ok(onboardingsResponse));
        when(tokenRestClient._getContractSigned(tokenId))
                .thenReturn(ResponseEntity.ok(expectedContract));

        Resource result = institutionV2Service.getContract(institutionId, productId);

        assertEquals(expectedContract, result);
    }

    @Test
    void getContract_shouldThrowException_whenNoActiveOnboarding() {
        String institutionId = "inst1";
        String productId = "prod1";

        OnboardingResponse onboarding = new OnboardingResponse();
        onboarding.setStatus(OnboardingResponse.StatusEnum.DELETED);

        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        onboardingsResponse.setOnboardings(List.of(onboarding));

        when(coreInstitutionApiRestClient._getOnboardingsInstitutionUsingGET(institutionId, productId))
                .thenReturn(ResponseEntity.ok(onboardingsResponse));

        assertThrows(ResourceNotFoundException.class, () ->
                institutionV2Service.getContract(institutionId, productId)
        );
    }

    @Test
    void getContract_shouldHandleNullResponseBody() {
        String institutionId = "inst1";
        String productId = "prod1";

        when(coreInstitutionApiRestClient._getOnboardingsInstitutionUsingGET(institutionId, productId))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(ResourceNotFoundException.class, () ->
                institutionV2Service.getContract(institutionId, productId)
        );
    }

    @Test
    void getContract_shouldReturnMostRecentActiveOnboarding() {
        String institutionId = "inst1";
        String productId = "prod1";
        String tokenId = "token-latest";
        Resource expectedContract = new ByteArrayResource("contract".getBytes());

        OnboardingResponse older = new OnboardingResponse();
        older.setStatus(OnboardingResponse.StatusEnum.ACTIVE);
        older.setCreatedAt(OffsetDateTime.now().minusDays(1));
        older.setTokenId("token-old");

        OnboardingResponse newer = new OnboardingResponse();
        newer.setStatus(OnboardingResponse.StatusEnum.ACTIVE);
        newer.setCreatedAt(OffsetDateTime.now());
        newer.setTokenId(tokenId);

        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        onboardingsResponse.setOnboardings(List.of(older, newer));

        when(coreInstitutionApiRestClient._getOnboardingsInstitutionUsingGET(institutionId, productId))
                .thenReturn(ResponseEntity.ok(onboardingsResponse));
        when(tokenRestClient._getContractSigned(tokenId))
                .thenReturn(ResponseEntity.ok(expectedContract));

        Resource result = institutionV2Service.getContract(institutionId, productId);

        assertEquals(expectedContract, result);
    }
}
