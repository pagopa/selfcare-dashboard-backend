package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.product.mapper.ProductMapper;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import it.pagopa.selfcare.product.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductConnectorImplTest extends BaseConnectorTest {

    @InjectMocks
    private ProductConnectorImpl productConnectorImpl;
    @Spy
    private ProductMapper productMapper;
    @Mock
    private ProductService productServiceMock;
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void GetProductById() throws IOException {

        String productId = "123";

        ClassPathResource resource = new ClassPathResource("stubs/Product.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Product product = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(productServiceMock.getProduct(productId)).thenReturn(product);

        Product result = productConnectorImpl.getProduct(productId);
        Assertions.assertEquals(product, result);
        Mockito.verify(productServiceMock, times(1)).getProduct(productId);
    }

    @Test
    void getProductByIdWithoutProductId() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> productConnectorImpl.getProduct(null));
        verify(productServiceMock, never()).getProduct(null);
    }

    @Test
    void GetProductByIdWithEmptyProduct() {

        String productId = "123";
        when(productServiceMock.getProduct(productId)).thenReturn(null);
        Product result = productConnectorImpl.getProduct(productId);
        assertNull(result);
        Mockito.verify(productServiceMock, times(1)).getProduct(productId);
    }


    @Test
    void getProductsWithProductEmpty() {

        when(productServiceMock.getProducts(false, true)).thenReturn(null);
        List<Product> result = productConnectorImpl.getProducts();
        assertNull(result);
        verify(productServiceMock, times(1)).getProducts(false, true);
    }

    @Test
    void GetProducts() throws IOException {

        ClassPathResource resource = new ClassPathResource("stubs/ProductList.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<Product> product = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(productServiceMock.getProducts(false, true)).thenReturn(product);

        List<Product> result = productConnectorImpl.getProducts();
        Assertions.assertEquals(product, result);
        Mockito.verify(productServiceMock, times(1)).getProducts(false, true);
    }

    @Test
    void getProductRoleMappingsWithoutProductId() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> productConnectorImpl.getProductRoleMappings(null));
        verify(productServiceMock, never()).getProduct(null);
    }

    @Test
    void getProductRoleMappingsWithEmptyProduct() {

        String productId = "123";
        when(productServiceMock.getProduct(productId)).thenReturn(null);
        Map<PartyRole, ProductRoleInfo> result = productConnectorImpl.getProductRoleMappings(productId);
        assertNull(result);
        Mockito.verify(productServiceMock, times(1)).getProduct(productId);
    }

    @Test
    void testGetProductRoleMappings() throws IOException {

        String productId = "123";

        ClassPathResource resource = new ClassPathResource("stubs/Product.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Product product = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(productServiceMock.getProduct(productId)).thenReturn(product);
        Map<PartyRole, ProductRoleInfo> result = productConnectorImpl.getProductRoleMappings(productId);
        Assertions.assertEquals(product.getRoleMappings(), result);
        Mockito.verify(productServiceMock, times(1)).getProduct(productId);
    }

    @Test
    void getProductsTreeWithEmptyProduct() {

        when(productServiceMock.getProducts(false, true)).thenReturn(null);
        List<ProductTree> result = productConnectorImpl.getProductsTree();
        assertNull(result);
        verify(productServiceMock, times(1)).getProducts(false, true);
    }


    @Test
    void getProductsTree() throws IOException {

        ClassPathResource resource = new ClassPathResource("stubs/ProductList.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<Product> product = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource productTreeResource = new ClassPathResource("stubs/ProductTree.json");
        byte[] productTreeStream = Files.readAllBytes(productTreeResource.getFile().toPath());
        List<ProductTree> productTrees = objectMapper.readValue(productTreeStream, new TypeReference<>() {
        });

        when(productServiceMock.getProducts(anyBoolean(), anyBoolean())).thenReturn(product);
        when(productMapper.toTreeResource(product)).thenReturn(productTrees);

        List<ProductTree> result = productConnectorImpl.getProductsTree();
        assertSame(productTrees, result);
        verify(productServiceMock, times(1)).getProducts(false, true);
    }
}

