package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.OnboardedProduct;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.rmi.AccessException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        InstitutionV2ServiceImpl.class
})
@TestPropertySource(locations = "classpath:config/core-config.properties")
class InstitutionV2ServiceImplTest {

    @Autowired
    private InstitutionV2ServiceImpl institutionV2Service;
    @MockBean
    private UserApiConnector userApiConnectorMock;
    @MockBean
    private MsCoreConnector msCoreConnectorMock;

    @Test
    void getInstitutionUser() {
        // given
        String institutionId = "institutionId";
        String loggedUserId = "loggedUserId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId("userId1");

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        userInfoMock1.setId("userId1");

        ProductInfo productInfo1 = new ProductInfo();
        String productId1 = "prod-1";
        productInfo1.setId(productId1);

        Map<String, ProductInfo> products1 = new HashMap<>();
        products1.put(productId1, productInfo1);
        userInfoMock1.setProducts(products1);

        User userMock = mockInstance(new User());
        userMock.setId("userId1");
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        userMock.setWorkContacts(workContact);

        userInfoMock1.setUser(userMock);

        when(userApiConnectorMock.getUsers(eq(institutionId), any(UserInfo.UserInfoFilter.class), eq(loggedUserId)))
                .thenReturn(List.of(userInfoMock1));

        // when
        UserInfo userInfo = institutionV2Service.getInstitutionUser(institutionId, userInfoFilter.getUserId(),
                loggedUserId);
        // then
        TestUtils.checkNotNullFields(userInfo);
        TestUtils.checkNotNullFields(userInfo.getUser());
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(1, userInfo.getProducts().size());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1)).getUsers(Mockito.eq(institutionId), filterCaptor.capture(),
                Mockito.eq(loggedUserId));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getRole());
        assertNull(capturedFilter.getProductId());
        assertNull(capturedFilter.getProductRoles());
        assertEquals(userInfoFilter.getUserId(), capturedFilter.getUserId());
    }

    @Test
    void getInstitutionUser_nullUserId() {
        // given
        String institutionId = "institutionId";
        String loggedUserId = "loggedUserId";
        String userId = null;
        // when
        Executable executable = () -> institutionV2Service.getInstitutionUser(institutionId, userId, loggedUserId);
        // then

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A user id is required", e.getMessage());
        verifyNoInteractions(userApiConnectorMock);
    }

    @Test
    void getInstitutionUser_userNotFound() {
        // given
        String institutionId = "institutionId";
        String userId = "userId1";
        String loggedUserId = "loggedUserId";

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        when(userApiConnectorMock.getUsers(any(), any(), any())).thenReturn(Collections.emptyList());
        // when
        Executable executable = () -> institutionV2Service.getInstitutionUser(institutionId, userId, loggedUserId);
        // then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        Assertions.assertEquals("No User found for the given userId", e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1)).getUsers(Mockito.eq(institutionId), filterCaptor.capture(),
                Mockito.eq(loggedUserId));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getRole());
        assertNull(capturedFilter.getProductId());
        assertNull(capturedFilter.getProductRoles());
        assertEquals(userId, capturedFilter.getUserId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userApiConnectorMock);
    }

    @Test
    void findInstitutionByIdTest2(){
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn("UserId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Institution institution = new Institution();
        institution.setExternalId("externalId");
        institution.setDescription("description");
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId("productId");
        institution.setOnboarding(Collections.singletonList(onboardedProduct));
        when(msCoreConnectorMock.getInstitution("institutionId")).thenReturn(institution);

        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(Collections.emptyList());
        when(userApiConnectorMock.getProducts("institutionId", "UserId")).thenReturn(userInstitution);

        Institution institutionResponse = institutionV2Service.findInstitutionById("institutionId");
        Assertions.assertEquals("description", institutionResponse.getDescription());
        Assertions.assertEquals("externalId", institutionResponse.getExternalId());
    }

    @Test
    void findInstitutionById(){
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn("UserId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Institution institution = new Institution();
        institution.setExternalId("externalId");
        institution.setDescription("description");
        OnboardedProduct onboardedInstitutionProduct = new OnboardedProduct();
        onboardedInstitutionProduct.setProductId("productId");
        OnboardedProduct onboardedInstitutionProduct2 = new OnboardedProduct();
        onboardedInstitutionProduct2.setProductId("productId2");
        institution.setOnboarding(List.of(onboardedInstitutionProduct, onboardedInstitutionProduct2));
        when(msCoreConnectorMock.getInstitution("institutionId")).thenReturn(institution);

        it.pagopa.selfcare.dashboard.connector.model.user.OnboardedProduct onboardedProduct1 = new it.pagopa.selfcare.dashboard.connector.model.user.OnboardedProduct();
        onboardedProduct1.setRole(MANAGER);
        onboardedProduct1.setProductId("productId");
        onboardedProduct1.setStatus(ACTIVE);
        it.pagopa.selfcare.dashboard.connector.model.user.OnboardedProduct onboardedProduct2 = new it.pagopa.selfcare.dashboard.connector.model.user.OnboardedProduct();
        onboardedProduct2.setRole(MANAGER);
        onboardedProduct2.setProductId("productId2");
        onboardedProduct2.setStatus(DELETED);
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(List.of(onboardedProduct1, onboardedProduct2));
        when(userApiConnectorMock.getProducts("institutionId", "UserId")).thenReturn(userInstitution);

        Institution institutionResponse = institutionV2Service.findInstitutionById("institutionId");
        Assertions.assertEquals("description", institutionResponse.getDescription());
        Assertions.assertEquals("externalId", institutionResponse.getExternalId());
        assertEquals(2, institution.getOnboarding().size());
        assertTrue(institution.getOnboarding().get(0).isAuthorized());
        assertFalse(institution.getOnboarding().get(1).isAuthorized());
    }

    @Test
    void findInstitutionById_shouldThrowAccessDeniedException(){
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(OPERATOR, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn("UserId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Assertions.assertThrows(AccessDeniedException.class, () -> institutionV2Service.findInstitutionById("institutionId"));
    }

    @Test
    void findInstitutionById_shouldThrowInstitutionNotFoundException(){
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(OPERATOR, "productRole", "productId");
        SelfCareUser principal = Mockito.mock(SelfCareUser.class);
        when(principal.getId()).thenReturn("UserId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        it.pagopa.selfcare.dashboard.connector.model.user.OnboardedProduct onboardedProduct1 = new it.pagopa.selfcare.dashboard.connector.model.user.OnboardedProduct();
        onboardedProduct1.setRole(MANAGER);
        onboardedProduct1.setProductId("productId");
        UserInstitution userInstitution = new UserInstitution();
        userInstitution.setProducts(List.of(onboardedProduct1));
        when(userApiConnectorMock.getProducts("institutionId", "UserId")).thenReturn(userInstitution);

        Assertions.assertThrows(ResourceNotFoundException.class, () -> institutionV2Service.findInstitutionById("institutionId"));
    }

}
