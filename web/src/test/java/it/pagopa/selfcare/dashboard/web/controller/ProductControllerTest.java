package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.core.ProductService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.product.ProductRoleMappingsResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collection;
import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebMvcTest(value = {ProductController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ProductController.class, WebTestConfig.class})
class ProductControllerTest {

    private static final String BASE_URL = "/products";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private ProductService productServiceMock;


    @Test
    void getProductRoles() throws Exception {
        // given
        String productId = "prod1";
        Mockito.when(productServiceMock.getProductRoles(Mockito.anyString()))
                .thenReturn(new EnumMap<PartyRole, ProductRoleInfo>(PartyRole.class) {{
                    put(PartyRole.MANAGER, new ProductRoleInfo());
                    put(PartyRole.OPERATOR, new ProductRoleInfo());
                }});
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{productId}/roles", productId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        // then
        Collection<ProductRoleMappingsResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(resources);
        Mockito.verify(productServiceMock, Mockito.times(1))
                .getProductRoles(productId);
        Mockito.verifyNoMoreInteractions(productServiceMock);
    }

}