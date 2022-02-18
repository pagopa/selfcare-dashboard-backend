package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductsConnector productsConnectorMock;

    @InjectMocks
    private ProductServiceImpl productService;


    @Test
    void getProductRoles_nullProductId() {
        // given
        String productId = null;
        // when
        Executable executable = () -> productService.getProductRoles(productId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock);
    }


    @Test
    void getProductRoles() {
        // given
        String productId = "productId";
        EnumMap<PartyRole, ProductRoleInfo> roleMappingsMocked = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, new ProductRoleInfo());
            put(PartyRole.DELEGATE, new ProductRoleInfo());
            put(PartyRole.SUB_DELEGATE, new ProductRoleInfo());
            put(PartyRole.OPERATOR, new ProductRoleInfo());
        }};
        Mockito.when(productsConnectorMock.getProductRoleMappings(Mockito.any()))
                .thenReturn(roleMappingsMocked);
        // when
        Map<PartyRole, ProductRoleInfo> roleMappings = productService.getProductRoles(productId);
        // then
        Assertions.assertSame(roleMappingsMocked, roleMappings);
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductRoleMappings(productId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
    }

}