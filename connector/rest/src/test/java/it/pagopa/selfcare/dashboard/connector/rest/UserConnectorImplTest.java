package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserPermissionRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapperImpl;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {UserConnectorImpl.class, InstitutionMapperImpl.class, UserMapper.class})
class UserConnectorImplTest {

    @Mock
    UserApiRestClient userApiRestClient;

    @Mock
    UserPermissionRestClient userPermissionRestClient;


    UserConnectorImpl userConnector;

    @Spy
    UserMapper userMapper = new UserMapperImpl();

    @BeforeEach
    void setup(){
        userConnector = new UserConnectorImpl(userApiRestClient, userPermissionRestClient, new InstitutionMapperImpl(), userMapper);
    }


    @Test
    void getUserProductsNotFound() {

        when(userApiRestClient._usersUserIdProductsGet("userID", null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()))).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> userConnector.getUserProducts("userID"));
    }

    @Test
    void getUserProductsFound() {
        UserProductsResponse userProductsResponse = getUserProductsResponse();
        when(userApiRestClient._usersUserIdProductsGet("userID", null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()))).thenReturn(ResponseEntity.ok(userProductsResponse));
        List<InstitutionInfo> result = userConnector.getUserProducts("userID");
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(ACTIVE, result.get(0).getStatus());
        Assertions.assertEquals("institutionId", result.get(0).getId());
        Assertions.assertEquals("institutionName", result.get(0).getDescription());
    }

    private static UserProductsResponse getUserProductsResponse() {
        UserProductsResponse userProductsResponse = new UserProductsResponse();
        userProductsResponse.setId("userID");
        InstitutionProducts institutionProducts = new InstitutionProducts();
        institutionProducts.setInstitutionId("institutionId");
        institutionProducts.setInstitutionName("institutionName");
        institutionProducts.setProducts(getOnboardedProduct());
        userProductsResponse.setBindings(List.of(institutionProducts));
        return userProductsResponse;
    }

    private static List<OnboardedProductResponse> getOnboardedProduct() {
        OnboardedProductResponse onboardedProductResponse = new OnboardedProductResponse();
        onboardedProductResponse.setProductId("prod-pagopa");
        onboardedProductResponse.setRole(PartyRole.MANAGER);
        onboardedProductResponse.setStatus(OnboardedProductState.ACTIVE);
        OnboardedProductResponse onboardedProductResponse2 = new OnboardedProductResponse();
        onboardedProductResponse2.setProductId("prod-pagopa");
        onboardedProductResponse2.setRole(PartyRole.MANAGER);
        onboardedProductResponse2.setStatus(OnboardedProductState.PENDING);
        return List.of(onboardedProductResponse, onboardedProductResponse2);
    }

    @Test
    void suspend() {

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        // when
        userConnector.suspendUserProduct(userId, institutionId, productId);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId,  OnboardedProductState.SUSPENDED);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void activate() {

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        // when
        userConnector.activateUserProduct(userId, institutionId, productId);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.ACTIVE);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void delete() {

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        // when
        userConnector.deleteUserProduct(userId, institutionId, productId);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.DELETED);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void getUserById(){

        //given
        String userId = "userId";
        UserDetailResponse userDetailResponse = mockInstance(new UserDetailResponse());
        when(userApiRestClient._usersIdDetailsGet(anyString())).thenReturn(new ResponseEntity<>(userDetailResponse, HttpStatus.OK));
        //when
        User user = userConnector.getUserById(userId);
        //then
        assertNotNull(user);
        verify(userApiRestClient, times(1))._usersIdDetailsGet(userId);
    }

    @Test
    void search(){
        //given
        String fiscalCode = "fiscalCode";
        UserDetailResponse userDetailResponse = mockInstance(new UserDetailResponse());
        when(userApiRestClient._usersSearchPost(any())).thenReturn(new ResponseEntity<>(userDetailResponse, HttpStatus.OK));
        //when
        User user = userConnector.searchByFiscalCode(fiscalCode);
        //then
        assertNotNull(user);
        ArgumentCaptor<SearchUserDto> searchUserDtoArgumentCaptor = ArgumentCaptor.forClass(SearchUserDto.class);
        verify(userApiRestClient, times(1))._usersSearchPost(searchUserDtoArgumentCaptor.capture());
        SearchUserDto captured = searchUserDtoArgumentCaptor.getValue();
        assertEquals(fiscalCode, captured.getFiscalCode());
    }

    @Test
    void hasPermissionTrue() {
        //given
        String institutionId = "institutionId";
        String permission = "ADMIN";
        String productId = "productId";
        when(userPermissionRestClient._authorizeInstitutionIdGet(institutionId, PermissionTypeEnum.ADMIN, productId)).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));
        //when
        Boolean result = userConnector.hasPermission(institutionId, permission, productId);
        //then
        assertNotNull(result);
        assertEquals(true, result);
    }

    @Test
    void hasPermissionFalse() {
        //given
        String institutionId = "institutionId";
        String permission = "ADMIN";
        String productId = "productId";
        when(userPermissionRestClient._authorizeInstitutionIdGet(institutionId, PermissionTypeEnum.ADMIN, productId)).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));
        //when
        Boolean result = userConnector.hasPermission(institutionId, permission, productId);
        //then
        assertNotNull(result);
        assertEquals(false, result);
    }

    @Test
    void update() {
        // given
        final String userId = "userId";
        final String institutionId = "institutionId";
        final var user = new MutableUserFieldsDto();
        // when
        userConnector.updateUser(userId, institutionId, user);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdUserRegistryPut(userId, institutionId, it.pagopa.selfcare.user.generated.openapi.v1.dto.MutableUserFieldsDto.builder().build());
        verifyNoMoreInteractions(userApiRestClient);
    }

}
