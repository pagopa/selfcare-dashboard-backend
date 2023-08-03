package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersPspResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokersResource;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.dashboard.connector.rest.config.PagoPABackOfficeRestClientTestConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(
        locations = "classpath:config/pago-pa-backoffice-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = PagoPABackOfficeRestClientTest.RandomPortInitializer.class,
        classes = {PagoPABackOfficeRestClientTestConfig.class, HttpClientConfiguration.class})
class PagoPABackOfficeRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/brokers"))
            .build();

    @Autowired
    private MsBackOfficeStationApiClient backOfficeStationApiClient;

    @Autowired
    private MsBackOfficeChannelApiClient backOfficeChannelApiClient;

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("PAGO_PA_BACKOFFICE_URL=%s",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }


    @Test
    void getBrokersEC() {
        // given and when
        ResponseEntity<BrokersResource> response = backOfficeStationApiClient._getBrokersECUsingGET( 1, 10, null, null, null, null);
        // then
        assertFalse(Objects.isNull(response));
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getBrokers().isEmpty());
        assertEquals("code", response.getBody().getBrokers().get(0).getBrokerCode());
    }


    @Test
    void getBrokersPSP() {
        // given and when
        ResponseEntity<BrokersPspResource> response = backOfficeChannelApiClient._getBrokersPspUsingGET( 1, 10, null, null, null, null);
        // then
        assertFalse(Objects.isNull(response));
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getBrokersPsp().isEmpty());
        assertEquals("code", response.getBody().getBrokersPsp().get(0).getBrokerPspCode());
    }

}