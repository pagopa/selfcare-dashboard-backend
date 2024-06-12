package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.ExchangedToken;
import it.pagopa.selfcare.dashboard.web.model.IdentityTokenResource;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenServiceV2;
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

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@WebMvcTest(value = {TokenV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {TokenV2Controller.class, WebTestConfig.class})
class TokenV2ControllerTest {

    private static final String BASE_URL = "/v2/token";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private ExchangeTokenServiceV2 exchangeTokenServiceMock;


    @Test
    void exchange() throws Exception {
        // given
        String institutionId = "inst1";
        String productId = "prod1";
        Mockito.when(exchangeTokenServiceMock.exchange(anyString(), anyString(), any()))
                .thenReturn(new ExchangedToken("token", "urlBO"));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/exchange")
                .param("institutionId", institutionId)
                .param("productId", productId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        IdentityTokenResource resource = objectMapper.readValue(result.getResponse().getContentAsString(), IdentityTokenResource.class);
        assertNotNull(resource);
        assertNotNull(resource.getToken());
        verify(exchangeTokenServiceMock, Mockito.times(1))
                .exchange(institutionId, productId, Optional.empty());
        verifyNoMoreInteractions(exchangeTokenServiceMock);
    }

    @Test
    void billingExchange() throws Exception {
        // given
        String institutionId = "inst1";
        Mockito.when(exchangeTokenServiceMock.retrieveBillingExchangedToken(anyString()))
                .thenReturn(new ExchangedToken("token", "urlBO"));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/exchange/fatturazione")
                        .param("institutionId", institutionId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        URI resource = objectMapper.readValue(result.getResponse().getContentAsString(), URI.class);
        assertNotNull(resource);
        verify(exchangeTokenServiceMock, Mockito.times(1))
                .retrieveBillingExchangedToken(institutionId);
        verifyNoMoreInteractions(exchangeTokenServiceMock);
    }


}