package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapperImpl;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {UserConnectorImpl.class, InstitutionMapperImpl.class, UserMapper.class})
class UserConnectorImplTest {

    @Mock
    UserApiRestClient userApiRestClient;


    UserConnectorImpl userConnector;


    @Test
    void getUserProductsNotFound() {
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionMapperImpl());
        when(userApiRestClient._usersUserIdProductsGet("userID", null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()))).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> userConnector.getUserProducts("userID"));
    }

    @Test
    void getUserProductsFound() {
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionMapperImpl());
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
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionMapperImpl());

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        // when
        userConnector.suspendUserProduct(userId, institutionId, productId);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdStatusPut(userId, institutionId, productId, null, null, OnboardedProductState.SUSPENDED);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void activate() {
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionMapperImpl());

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        // when
        userConnector.activateUserProduct(userId, institutionId, productId);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdStatusPut(userId, institutionId, productId, null, null, OnboardedProductState.ACTIVE);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void delete() {
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionMapperImpl());

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        // when
        userConnector.deleteUserProduct(userId, institutionId, productId);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdStatusPut(userId, institutionId, productId, null, null, OnboardedProductState.DELETED);
        verifyNoMoreInteractions(userApiRestClient);
    }


}
