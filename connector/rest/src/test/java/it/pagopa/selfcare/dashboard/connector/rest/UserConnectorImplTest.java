package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionV2MapperImpl;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapperImpl;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {UserConnectorImpl.class, InstitutionV2MapperImpl.class, UserMapper.class})
class UserConnectorImplTest {

    @Mock
    UserApiRestClient userApiRestClient;


    UserConnectorImpl userConnector;

    @Test
    void updateUserOK() {
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionV2MapperImpl(), new UserMapperImpl());
        when(userApiRestClient._usersIdUserRegistryPut(eq("userID"),eq("InstitutionId"), any(UserRegistryFieldsDto.class)))
                .thenReturn(ResponseEntity.ok().build());
        Assertions.assertDoesNotThrow(() -> userConnector.updateUser("userID", "InstitutionId", new MutableUserFieldsDto()));
    }

    @Test
    void updateUserKO() {
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionV2MapperImpl(), new UserMapperImpl());
        when(userApiRestClient._usersIdUserRegistryPut(eq("userID"),eq("InstitutionId"), any(UserRegistryFieldsDto.class)))
                .thenThrow(ResourceNotFoundException.class);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> userConnector.updateUser("userID", "InstitutionId", new MutableUserFieldsDto()));
    }

    @Test
    void getUserProductsNotFound() {
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionV2MapperImpl(), new UserMapperImpl());
        when(userApiRestClient._usersUserIdProductsGet("userID", null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()))).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> userConnector.getUserProducts("userID"));
    }

    @Test
    void getUserProductsFound() {
        userConnector = new UserConnectorImpl(userApiRestClient, new InstitutionV2MapperImpl(), new UserMapperImpl());
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
}
