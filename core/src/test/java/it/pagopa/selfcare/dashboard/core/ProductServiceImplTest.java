package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest extends BaseServiceTest {

    @InjectMocks
    ProductServiceImpl productService;
    @Mock
    private ProductsConnector productsConnectorMock;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void getProductRoles() {

        String productId = "productId";
        String institutionType = "type";
        Map<PartyRole, ProductRoleInfo> productRoleMappingsMock = new HashMap<>();
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        productRoleMappingsMock.put(PartyRole.MANAGER, productRoleInfo);

        when(productsConnectorMock.getProductRoleMappings(productId, institutionType)).thenReturn(productRoleMappingsMock);

        Map<PartyRole, ProductRoleInfo> result = productService.getProductRoles(productId, institutionType);
        Assertions.assertEquals(productRoleMappingsMock, result);
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProductRoleMappings(productId, institutionType);
    }

    @Test
    void getProductRolesWithoutProductId() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> productService.getProductRoles(null, null));
        Mockito.verifyNoInteractions(productsConnectorMock);
    }

}
