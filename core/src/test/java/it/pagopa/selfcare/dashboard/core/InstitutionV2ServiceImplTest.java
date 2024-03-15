package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.SUSPENDED;
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
    private ProductsConnector productServiceMock;

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

        Product product1 = mockInstance(new Product(), "setId");
        product1.setId(productId1);
        when(productServiceMock.getProduct(productId1))
                .thenReturn(product1);

        Map<String, Product> idToProductMap = Map.of(productId1, product1);

        // when
        UserInfo userInfo = institutionV2Service.getInstitutionUser(institutionId, userInfoFilter.getUserId(), loggedUserId);
        // then
        TestUtils.checkNotNullFields(userInfo);
        TestUtils.checkNotNullFields(userInfo.getUser());
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(1, userInfo.getProducts().size());
        for (String key : productInfoMap.keySet()) {
            ProductInfo productInfo = productInfoMap.get(key);
            Assertions.assertEquals(idToProductMap.get(productInfo.getId()).getTitle(), productInfo.getTitle());
        }
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture(), Mockito.eq(loggedUserId));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getRole());
        assertNull(capturedFilter.getProductId());
        assertNull(capturedFilter.getProductRoles());
        assertEquals(userInfoFilter.getUserId(), capturedFilter.getUserId());

        verify(productServiceMock, times(1))
                .getProduct(productId1);

        verifyNoMoreInteractions(userApiConnectorMock, productServiceMock);
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
        when(userApiConnectorMock.getUsers(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        // when
        Executable executable = () -> institutionV2Service.getInstitutionUser(institutionId, userId,loggedUserId);
        // then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        Assertions.assertEquals("No User found for the given userId", e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture(), Mockito.eq(loggedUserId));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getRole());
        assertNull(capturedFilter.getProductId());
        assertNull(capturedFilter.getProductRoles());
        assertEquals(userId, capturedFilter.getUserId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(productServiceMock, times(0))
                .getProduct(any());
        verifyNoMoreInteractions(userApiConnectorMock);
    }

}
