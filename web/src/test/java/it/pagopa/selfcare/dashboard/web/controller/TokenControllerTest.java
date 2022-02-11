package it.pagopa.selfcare.dashboard.web.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.TargetEnvironment;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.IdentityTokenResource;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
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
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebMvcTest(value = {TokenController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {TokenController.class, WebTestConfig.class})
@ExtendWith(SystemStubsExtension.class)
class TokenControllerTest {

    private static final String BASE_URL = "/token";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private ExchangeTokenService exchangeTokenServiceMock;

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @Test
    void exchange() throws Exception {
        // given
        String institutionId = "inst1";
        String productId = "prod1";
        String realm = "r";
        Mockito.when(exchangeTokenServiceMock.exchange(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("token");
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/exchange")
                .param("institutionId", institutionId)
                .param("productId", productId)
                .param("realm", realm)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        IdentityTokenResource resource = objectMapper.readValue(result.getResponse().getContentAsString(), IdentityTokenResource.class);
        assertNotNull(resource);
        assertNotNull(resource.getToken());
        Mockito.verify(exchangeTokenServiceMock, Mockito.times(1))
                .exchange(institutionId, productId, realm);
        Mockito.verifyNoMoreInteractions(exchangeTokenServiceMock);
    }

    @Test
    void exchange_doLog() throws Exception {
        // given
        String institutionId = "inst1";
        String productId = "prod1";
        String realm = "r";
        Mockito.when(exchangeTokenServiceMock.exchange(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("token");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(listAppender);
        rootLogger.setLevel(Level.DEBUG);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/exchange")
                .param("institutionId", institutionId)
                .param("productId", productId)
                .param("realm", realm)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        Assertions.assertEquals(1, listAppender.list.stream()
                .filter(iLoggingEvent -> Level.DEBUG.equals(iLoggingEvent.getLevel())
                        && TokenController.class.getName().equals(iLoggingEvent.getLoggerName()))
                .count());
    }

    @Test
    void exchange_doNotLog() throws Exception {
        // given
        environmentVariables.set("ENV_TARGET", TargetEnvironment.PROD);

        String institutionId = "inst1";
        String productId = "prod1";
        String realm = "r";
        Mockito.when(exchangeTokenServiceMock.exchange(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("token");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(listAppender);
        rootLogger.setLevel(Level.DEBUG);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/exchange")
                .param("institutionId", institutionId)
                .param("productId", productId)
                .param("realm", realm)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        Assertions.assertEquals(1, listAppender.list.stream()
                .filter(iLoggingEvent -> Level.DEBUG.equals(iLoggingEvent.getLevel())
                        && TokenController.class.getName().equals(iLoggingEvent.getLoggerName()))
                .count());
    }

}