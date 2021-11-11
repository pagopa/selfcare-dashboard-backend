package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.dashboard.connector.model.organization.Organization;
import it.pagopa.selfcare.dashboard.connector.rest.config.PartyManagementRestClientTestConfig;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.EnumMap;
import java.util.Map;


@TestPropertySource(
        locations = "classpath:config/party-mgmt-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-integration-rest"
        })
@ContextConfiguration(
        initializers = PartyManagementRestClientTest.RandomPortInitializer.class,
        classes = {PartyManagementRestClientTestConfig.class})
public class PartyManagementRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    static {
        WireMockConfiguration config = RestTestUtils.getWireMockConfiguration("stubs/party-mgmt");
        wireMockRule = new WireMockClassRule(config);
    }


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_MANAGEMENT_URL=http://%s:%d/pdnd-interop-uservice-party-management/0.1",
                            wireMockRule.getOptions().bindAddress(),
                            wireMockRule.port())
            );
        }
    }


    private enum TestCase {
        FULLY_VALUED
    }

    private static final Map<PartyManagementRestClientTest.TestCase, String> testCase2instIdMap = new EnumMap<>(PartyManagementRestClientTest.TestCase.class) {{
        put(PartyManagementRestClientTest.TestCase.FULLY_VALUED, "organizationId_1");
    }};

    @Autowired
    private PartyManagementRestClient restClient;


    @Test
    public void getOrganization() {
        // given
        String organizationId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        // when
        Organization response = restClient.getOrganization(organizationId);
        // then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getDescription());
        Assert.assertNotNull(response.getManagerName());
        Assert.assertNotNull(response.getManagerSurname());
        Assert.assertNotNull(response.getDigitalAddress());
        Assert.assertNotNull(response.getPartyId());
        Assert.assertNotNull(response.getAttributes());
    }

}