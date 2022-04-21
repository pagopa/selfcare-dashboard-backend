package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.web.model.ProductRoleMappingsResource;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    @Test
    void toResource_notNull() {
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
    void toResource_null() {
        // given
        Product model = null;
        // when
        ProductsResource resource = ProductsMapper.toResource(model);
        // then
        assertNull(resource);
    }


    @Test
    void toProductRoleResource_null() {
        // given
        ProductRoleInfo.ProductRole input = null;
        // when
        ProductRoleMappingsResource.ProductRoleResource output = ProductsMapper.toProductRoleResource(input);
        // then
        assertNull(output);
    }


    @Test
    void toProductRoleResource_notNull() {
        // given
        ProductRoleInfo.ProductRole input = TestUtils.mockInstance(new ProductRoleInfo.ProductRole());
        // when
        ProductRoleMappingsResource.ProductRoleResource output = ProductsMapper.toProductRoleResource(input);
        // then
        assertNotNull(output);
        assertEquals(input.getCode(), output.getCode());
        assertEquals(input.getLabel(), output.getLabel());
        assertEquals(input.getDescription(), output.getDescription());
    }


    @Test
    void toProductRoleMappingsResource_fromEntry_null() {
        // given
        Map.Entry<PartyRole, ProductRoleInfo> input = null;
        // when
        ProductRoleMappingsResource output = ProductsMapper.toProductRoleMappingsResource(input);
        // then
        assertNull(output);
    }


    @Test
    void toProductRoleMappingsResource_fromEntry_nullRoles() {
        // given
        Map.Entry<PartyRole, ProductRoleInfo> input = Map.entry(PartyRole.DELEGATE, TestUtils.mockInstance(new ProductRoleInfo()));
        // when
        ProductRoleMappingsResource output = ProductsMapper.toProductRoleMappingsResource(input);
        // then
        assertNotNull(output);
        assertNull(output.getProductRoles());
    }


    @Test
    void toProductRoleMappingsResource_fromEntry_notNull() {
        // given
        ProductRoleInfo productRoleInfo = TestUtils.mockInstance(new ProductRoleInfo(), "setRoles");
        productRoleInfo.setRoles(List.of(TestUtils.mockInstance(new ProductRoleInfo.ProductRole())));
        Map.Entry<PartyRole, ProductRoleInfo> input = Map.entry(PartyRole.DELEGATE, productRoleInfo);
        // when
        ProductRoleMappingsResource output = ProductsMapper.toProductRoleMappingsResource(input);
        // then
        assertNotNull(output);
        assertNotNull(output.getProductRoles());
        assertEquals(productRoleInfo.getRoles().size(), output.getProductRoles().size());
        assertEquals(input.getKey(), output.getPartyRole());
        assertEquals(input.getKey().getSelfCareAuthority(), output.getSelcRole());
        assertEquals(input.getValue().isMultiroleAllowed(), output.isMultiroleAllowed());
    }


    @Test
    void toProductRoleMappingsResource_fromMap_null() {
        // given
        Map<PartyRole, ProductRoleInfo> input = null;
        // when
        Collection<ProductRoleMappingsResource> output = ProductsMapper.toProductRoleMappingsResource(input);
        // then
        assertNull(output);
    }


    @Test
    void toProductRoleMappingsResource_fromMap_notNull() {
        // given
        EnumMap<PartyRole, ProductRoleInfo> input = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, TestUtils.mockInstance(new ProductRoleInfo(), 1));
            put(PartyRole.OPERATOR, TestUtils.mockInstance(new ProductRoleInfo(), 2));
        }};
        // when
        Collection<ProductRoleMappingsResource> output = ProductsMapper.toProductRoleMappingsResource(input);
        // then
        assertNotNull(output);
        assertEquals(input.size(), output.size());
    }

}