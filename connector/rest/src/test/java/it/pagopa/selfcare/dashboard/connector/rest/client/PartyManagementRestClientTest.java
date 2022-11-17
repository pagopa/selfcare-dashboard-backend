package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.dashboard.connector.rest.config.PartyManagementRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.relationship.Relationship;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestPropertySource(
        locations = "classpath:config/party-management-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = PartyManagementRestClientTest.RandomPortInitializer.class,
        classes = {PartyManagementRestClientTestConfig.class, HttpClientConfiguration.class})
class PartyManagementRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/party-management"))
            .build();


    static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_MANAGEMENT_URL=%s/pdnd-interop-uservice-party-management/0.0.1",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    @Autowired
    private PartyManagementRestClient restClient;


    @Test
    void getRelationshipById_fullyValued() {
        // given
        final UUID relationshipId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        // when
        final Relationship response = restClient.getRelationshipById(relationshipId);
        // then
        assertNotNull(response);
        checkNotNullFields(response);
        checkNotNullFields(response.getProduct());
        checkNotNullFields(response.getBilling());
        checkNotNullFields(response.getInstitutionUpdate());
        checkNotNullFields(response.getInstitutionUpdate().getPaymentServiceProvider());
        checkNotNullFields(response.getInstitutionUpdate().getDataProtectionOfficer());
    }

}