package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.core.ProductService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.model.product.ProductRoleMappingsResource;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.util.Collection;
import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @MockBean
    private ExchangeTokenService exchangeTokenServiceMock;


    @Test
    void getProductRoles() throws Exception {
        // given
        String productId = "prod1";
        when(productServiceMock.getProductRoles(anyString()))
                .thenReturn(new EnumMap<PartyRole, ProductRoleInfo>(PartyRole.class) {{
                    put(PartyRole.MANAGER, new ProductRoleInfo());
                    put(PartyRole.OPERATOR, new ProductRoleInfo());
                }});
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{productId}/roles", productId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        Collection<ProductRoleMappingsResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(resources);
        verify(productServiceMock, times(1))
                .getProductRoles(productId);
        verifyNoMoreInteractions(productServiceMock);
        verifyNoInteractions(exchangeTokenServiceMock);
    }


    @Test
    void retrieveProductBackoffice() throws Exception {
        // given
        String productId = "prod1";
        String institutionId = "inst1";
        final String identityToken = "identityToken";
        final String backOfficeUrl = "back-office-url#token=";
        when(exchangeTokenServiceMock.exchange(any(), any()))
                .thenReturn(new ExchangedToken(identityToken, backOfficeUrl + "<IdentityToken>"));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{productId}/back-office", productId)
                        .queryParam("institutionId", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        URI response = objectMapper.readValue(result.getResponse().getContentAsString(),URI.class);
        assertTrue(response.toString().contains(identityToken));
        assertTrue(response.toString().contains(backOfficeUrl));
        verify(exchangeTokenServiceMock, times(1))
                .exchange(institutionId, productId);
        verifyNoMoreInteractions(exchangeTokenServiceMock);
        verifyNoInteractions(productServiceMock);
    }
}
