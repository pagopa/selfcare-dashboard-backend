package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.core.ProductService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.model.mapper.BrokerResourceMapperImpl;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenServiceV2;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {ProductV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ProductV2Controller.class, WebTestConfig.class, BrokerResourceMapperImpl.class})
class ProductV2ControllerTest {
    private static final String BASE_URL = "/v2/products";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private ProductService productServiceMock;

    @MockBean
    private ExchangeTokenServiceV2 exchangeTokenServiceMock;

    @Test
    void retrieveProductBackoffice() throws Exception {
        // given
        String productId = "prod1";
        String institutionId = "inst1";
        final String identityToken = "identityToken";
        final String backOfficeUrl = "back-office-url#token=";
        when(exchangeTokenServiceMock.exchange(institutionId, productId, Optional.empty(), null))
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
        URI response = objectMapper.readValue(result.getResponse().getContentAsString(), URI.class);
        assertTrue(response.toString().contains(identityToken));
        assertTrue(response.toString().contains(backOfficeUrl));

        verify(exchangeTokenServiceMock, times(1))
                .exchange(institutionId, productId, Optional.empty(), null);
        verifyNoMoreInteractions(exchangeTokenServiceMock);
        verifyNoInteractions(productServiceMock);
    }

}
