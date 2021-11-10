package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.config.PartyProcessRestClientTestConfig;
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
        locations = "classpath:config/party-process-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-integration-rest"
        })
@ContextConfiguration(
        initializers = PartyProcessRestClientTest.RandomPortInitializer.class,
        classes = {PartyProcessRestClientTestConfig.class})
public class PartyProcessRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    static {
        WireMockConfiguration config = RestTestUtils.getWireMockConfiguration("stubs/party-process");
        wireMockRule = new WireMockClassRule(config);
    }


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_PROCESS_URL=http://%s:%d/pdnd-interop-uservice-party-process/0.0.1",
                            wireMockRule.getOptions().bindAddress(),
                            wireMockRule.port())
            );
        }
    }


    private enum TestCase {
        FULLY_VALUED,
        FULLY_NULL,
        EMPTY_RESULT
    }

    private static final Map<TestCase, String> testCase2instIdMap = new EnumMap<>(TestCase.class) {{
        put(TestCase.FULLY_VALUED, "institutionId1");
        put(TestCase.FULLY_NULL, "institutionId2");
        put(TestCase.EMPTY_RESULT, "institutionId3");
    }};

    @Autowired
    private PartyProcessRestClient restClient;


    @Test
    public void getInstitutionRelationships_fullyValued() {
        // given and when
        RelationshipsResponse response = restClient.getInstitutionRelationships(testCase2instIdMap.get(TestCase.FULLY_VALUED));
        // then
        Assert.assertNotNull(response);
        Assert.assertFalse(response.isEmpty());
        Assert.assertNotNull(response.get(0).getFrom());
        Assert.assertEquals(RelationshipInfo.RoleEnum.MANAGER, response.get(0).getRole());
        Assert.assertEquals(RelationshipInfo.StatusEnum.PENDING, response.get(0).getStatus());
        Assert.assertNotNull(response.get(0).getPlatformRole());
    }


    @Test
    public void getInstitutionRelationships_fullyNull() {
        // given and when
        RelationshipsResponse response = restClient.getInstitutionRelationships(testCase2instIdMap.get(TestCase.FULLY_NULL));
        // then
        Assert.assertNotNull(response);
        Assert.assertFalse(response.isEmpty());
        Assert.assertNull(response.get(0).getFrom());
        Assert.assertNull(response.get(0).getRole());
        Assert.assertNull(response.get(0).getStatus());
        Assert.assertNull(response.get(0).getPlatformRole());
    }


    @Test
    public void getInstitutionRelationships_emptyResult() {
        // given and when
        RelationshipsResponse response = restClient.getInstitutionRelationships(testCase2instIdMap.get(TestCase.EMPTY_RESULT));
        // then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isEmpty());
    }


    @Test
    public void getOnBoardingInfo_fullyValued() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_VALUED));
        // then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPerson());
        Assert.assertNotNull(response.getInstitutions());
        Assert.assertNotNull(response.getPerson().getName());
        Assert.assertNotNull(response.getPerson().getSurname());
        Assert.assertNotNull(response.getPerson().getTaxCode());
        Assert.assertNotNull(response.getInstitutions().get(0).getDescription());
        Assert.assertNotNull(response.getInstitutions().get(0).getDigitalAddress());
        Assert.assertNotNull(response.getInstitutions().get(0).getPlatformRole());
        Assert.assertNotNull(response.getInstitutions().get(0).getRole());
        Assert.assertNotNull(response.getInstitutions().get(0).getStatus());
        Assert.assertNotNull(response.getInstitutions().get(0).getAttributes());
    }

    @Test
    public void getOnBoardingInfo_fullyNull() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_NULL));
        // then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPerson());
        Assert.assertNotNull(response.getInstitutions());
        Assert.assertNull(response.getPerson().getName());
        Assert.assertNull(response.getPerson().getSurname());
        Assert.assertNull(response.getPerson().getTaxCode());
        Assert.assertNull(response.getInstitutions().get(0).getDescription());
        Assert.assertNull(response.getInstitutions().get(0).getDigitalAddress());
        Assert.assertNull(response.getInstitutions().get(0).getPlatformRole());
        Assert.assertNull(response.getInstitutions().get(0).getRole());
        Assert.assertNull(response.getInstitutions().get(0).getStatus());
        Assert.assertNull(response.getInstitutions().get(0).getAttributes());
    }

    @Test
    public void getOnBoardingInfo_emptyResult() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.EMPTY_RESULT));
        // then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getInstitutions().isEmpty());
        Assert.assertNull(response.getPerson());
    }

}