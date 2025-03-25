package it.pagopa.selfcare.dashboard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private it.pagopa.selfcare.product.service.ProductService productServiceExternal;



    @BeforeEach
    public void init() {
        super.setUp();
    }

    @Test
    void getProductRoles() throws IOException {

        String productId = "productId";
        String institutionType = "type";
        Map<PartyRole, ProductRoleInfo> productRoleMappingsMock = new HashMap<>();
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        productRoleMappingsMock.put(PartyRole.MANAGER, productRoleInfo);
        ClassPathResource productResource = new ClassPathResource("stubs/Product.json");
        byte[] resourceStreamInstitution = Files.readAllBytes(productResource.getFile().toPath());
        Product product = objectMapper.readValue(resourceStreamInstitution, new TypeReference<>() {
        });

        when(productServiceExternal.getProduct(productId)).thenReturn(product);

        Map<PartyRole, ProductRoleInfo> result = productService.getProductRoles(productId, institutionType);
        Assertions.assertEquals(productRoleMappingsMock.get("MANAGER"), result.get("MANAGER"));
        Mockito.verify(productServiceExternal, Mockito.times(1)).getProduct(productId);
    }

    @Test
    void getProductRolesWithoutProductId() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> productService.getProductRoles(null, null));
        Mockito.verifyNoInteractions(productServiceExternal);
    }

}
