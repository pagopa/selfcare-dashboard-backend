package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.product.mapper.ProductMapper;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductStatus;
import it.pagopa.selfcare.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ProductConnectorImpl.class})
@ExtendWith(SpringExtension.class)
class ProductConnectorImplTest {
    @Autowired
    private ProductConnectorImpl productConnectorImpl;
    @MockBean
    private ProductMapper productMapper;

    @MockBean
    private ProductService productService;

    @Test
    void testGetProductById() {
        Product product = dummyProduct();
        when(productService.getProduct(any())).thenReturn(product);
        assertSame(product, productConnectorImpl.getProduct("42"));
        verify(productService).getProduct(any());
    }

    @Test
    void testGetProducts() {
        Product product = dummyProduct();
        when(productService.getProducts(anyBoolean(),anyBoolean())).thenReturn(List.of(product));
        assertSame(product, productConnectorImpl.getProducts().get(0));
        verify(productService).getProducts(anyBoolean(),anyBoolean());
    }

    @Test
    void testGetProductRoleMappings() {
        Product product = dummyProduct();
        when(productService.getProduct(any())).thenReturn(product);
        assertSame(product.getRoleMappings(), productConnectorImpl.getProductRoleMappings("42"));
        verify(productService).getProduct(any());
    }

    @Test
    void testGetProductRoleMappings_noProduct() {
        when(productService.getProduct(any())).thenReturn(null);
        assertSame(null, productConnectorImpl.getProductRoleMappings("42"));
        verify(productService).getProduct(any());
    }

    @Test
    void getProductsTree() {
        Product product = dummyProduct();
        when(productService.getProducts(anyBoolean(),anyBoolean())).thenReturn(List.of(product));
        when(productMapper.toTreeResource(List.of(product))).thenReturn(List.of(new ProductTree()));
        productConnectorImpl.getProductsTree();
        verify(productService).getProducts(anyBoolean(),anyBoolean());
        verify(productMapper).toTreeResource(anyList());
    }

    private Product dummyProduct(){
        Product product = new Product();
        product.setContractTemplatePath("Contract Template Path");
        product.setContractTemplateVersion("1.0.2");
        product.setId("42");
        product.setParentId("42");
        product.setRoleMappings(null);
        product.setStatus(ProductStatus.ACTIVE);
        product.setTitle("Dr");
        return product;
    }
}

