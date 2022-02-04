package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductMapperTest {

    @Test
    void toResourceNotNull() {
        // given
        Product product = TestUtils.mockInstance(new Product());
        // when
        ProductsResource resource = ProductsMapper.toResource(product);
        // then
        assertEquals(product.getId(), resource.getId());
        assertEquals(product.getLogo(), resource.getLogo());
        assertEquals(product.getTitle(), resource.getTitle());
        assertEquals(product.getDescription(), resource.getDescription());
        assertEquals(product.getUrlPublic(), resource.getUrlPublic());
        assertEquals(product.getUrlBO(), resource.getUrlBO());
        assertEquals(product.getActivatedAt(), resource.getActivatedAt());
        assertEquals(product.getStatus(), resource.getStatus());
        assertEquals(product.isAuthorized(), resource.isAuthorized());
        assertEquals(product.getUserRole(), resource.getUserRole());
        TestUtils.reflectionEqualsByName(product, resource);
    }


    @Test
    void toResourceNull() {
        // given and when
        ProductsResource resource = ProductsMapper.toResource(null);
        // then
        assertNull(resource);
    }

}