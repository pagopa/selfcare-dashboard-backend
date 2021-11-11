package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.core.ProductsService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(value = {DashboardController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {DashboardController.class, WebTestConfig.class})
class DashboardControllerTest {

    private static final String BASE_URL = "/dashboard";
    private static final Product PRODUCT = TestUtils.mockInstance(new Product());

    @MockBean
    private ProductsService productsService;

    @MockBean
    private PartyConnector partyConnectorMock;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Test
    void getOnBoardingInfoNotNull() throws Exception {
        // given
        Mockito.when(partyConnectorMock.getOnBoardingInfo(Mockito.anyString()))
                .thenAnswer(invocationOnMock -> {
                    String id = invocationOnMock.getArgument(0, String.class);
                    InstitutionInfo institutionInfo = new InstitutionInfo();
                    institutionInfo.setInstitutionId(id);
                    OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
                    onBoardingInfo.setInstitutions(List.of(institutionInfo));
                    return onBoardingInfo;
                });
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/institutions/institutionId")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        InstitutionResource resource = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionResource.class);
        assertNotNull(resource);
    }

    @Test
    void getOnBoardingInfoNull() throws Exception {
        // given
        Mockito.when(partyConnectorMock.getOnBoardingInfo(Mockito.anyString()))
                .thenReturn(null);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/institutions/institutionId")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        assertEquals("", result.getResponse().getContentAsString());
    }


    @Test
    void getProductsNotNull() throws Exception {
        // given
        Mockito.when(productsService.getProducts())
                .thenReturn(Collections.singletonList(PRODUCT));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/products")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<ProductsResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    void getProductsNull() throws Exception {
        // given
        Mockito.when(productsService.getProducts())
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/products")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<ProductsResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

}