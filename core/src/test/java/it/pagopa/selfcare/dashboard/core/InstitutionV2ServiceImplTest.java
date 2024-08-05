package it.pagopa.selfcare.dashboard.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.OnboardingConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitutionWithActionsDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.PENDING;
import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.TOBEVALIDATED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class InstitutionV2ServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private InstitutionV2ServiceImpl institutionV2Service;
    @Mock
    private UserApiConnector userApiConnectorMock;
    @Mock
    private MsCoreConnector msCoreConnectorMock;
    @Mock
    private OnboardingConnector onboardingConnectorMock;

    @BeforeEach
    public void init() {
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

        when(userApiConnectorMock.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(Collections.emptyList());
        assertThrows(ResourceNotFoundException.class, () -> institutionV2Service.getInstitutionUser(institutionId, userId, loggedUserId));
    }

    @Test
    void getInstitutionUser() throws IOException {

        String institutionId = "institutionId";
        String userId = "userId";
        String loggedUserId = "loggedUserId";

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);

        ClassPathResource resource = new ClassPathResource("expectations/UserInfo.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserInfo userInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiConnectorMock.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(Collections.singletonList(userInfo));

        UserInfo actualUserInfo = institutionV2Service.getInstitutionUser("institutionId", "userId", "loggedUserId");
        assertEquals(userInfo, actualUserInfo);
        Mockito.verify(userApiConnectorMock, Mockito.times(1)).getUsers(institutionId, userInfoFilter, loggedUserId);
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
        when(userApiConnectorMock.getUserInstitutionWithActions(institutionId, userId, null)).thenReturn(null);
        assertThrows(AccessDeniedException.class, () -> institutionV2Service.findInstitutionById(institutionId));
        Mockito.verify(userApiConnectorMock, Mockito.times(1)).getUserInstitutionWithActions(institutionId, userId, null);
    }

    @Test
    void findInstitutionByIdNoInstitution() {

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

        UserInstitutionWithActionsDto userInstitutionWithActionsDto = new UserInstitutionWithActionsDto();

        when(userApiConnectorMock.getUserInstitutionWithActions(institutionId, userId, null)).thenReturn(userInstitutionWithActionsDto);
        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> institutionV2Service.findInstitutionById(institutionId));
    }

    @Test
    void findInstitutionByIdNoOnboarding() {

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

        UserInstitutionWithActionsDto userInstitutionWithActionsDto = new UserInstitutionWithActionsDto();
        Institution institution = new Institution();

        when(userApiConnectorMock.getUserInstitutionWithActions(institutionId, userId, null)).thenReturn(userInstitutionWithActionsDto);
        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);
        assertThrows(ResourceNotFoundException.class, () -> institutionV2Service.findInstitutionById(institutionId));
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
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ClassPathResource userInstitutionResource = new ClassPathResource("expectations/UserInstitutionWithActions.json");
        byte[] userInstitutionStream = Files.readAllBytes(userInstitutionResource.getFile().toPath());
        UserInstitutionWithActionsDto userInstitutionWithActionsDto = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });

        ClassPathResource resource = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiConnectorMock.getUserInstitutionWithActions(institutionId, userId, null)).thenReturn(userInstitutionWithActionsDto);
        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        Institution result = institutionV2Service.findInstitutionById(institutionId);
        Assertions.assertNotNull(result.getOnboarding().get(0).getUserProductActions());
        Assertions.assertEquals(2, result.getOnboarding().get(0).getUserProductActions().size());
        Mockito.verify(userApiConnectorMock, Mockito.times(1)).getUserInstitutionWithActions(institutionId, userId, null);
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
        UserInstitutionWithActionsDto userInstitutionWithActionsDto = objectMapper.readValue(userInstitutionStream, new TypeReference<>() {
        });

        ClassPathResource resource = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiConnectorMock.getUserInstitutionWithActions(institutionId, userId, null)).thenReturn(userInstitutionWithActionsDto);
        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        Institution result = institutionV2Service.findInstitutionById(institutionId);
        Assertions.assertEquals("LIMITED", result.getOnboarding().get(0).getUserRole());
        Mockito.verify(userApiConnectorMock, Mockito.times(1)).getUserInstitutionWithActions(institutionId, userId, null);
    }

    @Test
    void getPendingOnboarding_pendingFound() {
        // Given
        when(onboardingConnectorMock.getOnboardingWithFilter("test-institution", "test-product", PENDING.name()))
                .thenReturn(Boolean.TRUE);

        // When
        Boolean actualResponse = institutionV2Service.verifyIfExistsPendingOnboarding("test-institution", "test-product");

        // Then
        assertTrue(actualResponse);
        Mockito.verify(onboardingConnectorMock, Mockito.times(1))
                .getOnboardingWithFilter("test-institution", "test-product", PENDING.name());
    }

    @Test
    void getPendingOnboarding_pendingNotFound_tobeValidatedFound() {
        // Given
        when(onboardingConnectorMock.getOnboardingWithFilter("test-institution", "test-product", PENDING.name()))
                .thenReturn(Boolean.FALSE);
        when(onboardingConnectorMock.getOnboardingWithFilter("test-institution", "test-product", TOBEVALIDATED.name()))
                .thenReturn(Boolean.TRUE);

        // When
        Boolean actualResponse = institutionV2Service.verifyIfExistsPendingOnboarding("test-institution", "test-product");

        // Then
        assertTrue(actualResponse);
        Mockito.verify(onboardingConnectorMock, Mockito.times(1))
                .getOnboardingWithFilter("test-institution", "test-product", PENDING.name());
        Mockito.verify(onboardingConnectorMock, Mockito.times(1))
                .getOnboardingWithFilter("test-institution", "test-product", TOBEVALIDATED.name());
    }

    @Test
    void getPendingOnboarding_neitherFound() {
        // Given
        when(onboardingConnectorMock.getOnboardingWithFilter("test-institution", "test-product", PENDING.name()))
                .thenReturn(Boolean.FALSE);
        when(onboardingConnectorMock.getOnboardingWithFilter("test-institution", "test-product", TOBEVALIDATED.name()))
                .thenReturn(Boolean.FALSE);

        // When
        Boolean actualResponse = institutionV2Service.verifyIfExistsPendingOnboarding("test-institution", "test-product");

        // Then
        assertFalse(actualResponse);
        Mockito.verify(onboardingConnectorMock, Mockito.times(1))
                .getOnboardingWithFilter("test-institution", "test-product", PENDING.name());
        Mockito.verify(onboardingConnectorMock, Mockito.times(1))
                .getOnboardingWithFilter("test-institution", "test-product", TOBEVALIDATED.name());
    }
}
