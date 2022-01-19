package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.rest.config.PartyProcessRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.*;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.User;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.*;

import static it.pagopa.selfcare.dashboard.connector.rest.model.PartyRole.MANAGER;
import static it.pagopa.selfcare.dashboard.connector.rest.model.PartyRole.OPERATOR;
import static it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipState.PENDING;

@TestPropertySource(
        locations = "classpath:config/party-process-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest"
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
        // given
        String institutionId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        EnumSet<PartyRole> roles = null;
        EnumSet<RelationshipState> states = null;
        Set<String> products = null;
        // when
        RelationshipsResponse response = restClient.getInstitutionRelationships(institutionId, roles, states, products);
        // then
        Assert.assertNotNull(response);
        Assert.assertFalse(response.isEmpty());
        Assert.assertNotNull(response.get(0).getId());
        Assert.assertNotNull(response.get(0).getFrom());
        Assert.assertNotNull(response.get(0).getName());
        Assert.assertNotNull(response.get(0).getSurname());
        Assert.assertNotNull(response.get(0).getEmail());
        Assert.assertNotNull(response.get(0).getRole());
        Assert.assertNotNull(response.get(0).getState());
        Assert.assertNotNull(response.get(0).getCreatedAt());
        Assert.assertNotNull(response.get(0).getUpdatedAt());
        Assert.assertNotNull(response.get(0).getProduct());
        Assert.assertNotNull(response.get(0).getProduct().getId());
        Assert.assertNotNull(response.get(0).getProduct().getRole());
        Assert.assertNotNull(response.get(0).getProduct().getCreatedAt());
    }


    @Test
    public void getInstitutionRelationships_fullyNull() {
        // given
        String institutionId = testCase2instIdMap.get(TestCase.FULLY_NULL);
        EnumSet<PartyRole> roles = null;
        EnumSet<RelationshipState> states = null;
        Set<String> products = null;
        // when
        RelationshipsResponse response = restClient.getInstitutionRelationships(institutionId, roles, states, products);
        // then
        Assert.assertNotNull(response);
        Assert.assertFalse(response.isEmpty());
        Assert.assertNull(response.get(0).getId());
        Assert.assertNull(response.get(0).getFrom());
        Assert.assertNull(response.get(0).getRole());
        Assert.assertNull(response.get(0).getProduct());
        Assert.assertNull(response.get(0).getState());
    }


    @Test
    public void getInstitutionRelationships_emptyResult() {
        // given
        String institutionId = testCase2instIdMap.get(TestCase.EMPTY_RESULT);
        EnumSet<PartyRole> roles = EnumSet.of(MANAGER, OPERATOR);
        EnumSet<RelationshipState> states = EnumSet.of(ACTIVE, PENDING);
        Set<String> products = Set.of("prod1", "prod2");
        // when
        RelationshipsResponse response = restClient.getInstitutionRelationships(institutionId, roles, states, products);
        // then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isEmpty());
    }


    @Test
    public void getInstitutionProducts_fullyValued() {
        // given and when
        Products response = restClient.getInstitutionProducts(testCase2instIdMap.get(TestCase.FULLY_VALUED), null);
        // then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getProducts());
        Assert.assertFalse(response.getProducts().isEmpty());
        Assert.assertNotNull(response.getProducts().get(0).getId());
        Assert.assertNotNull(response.getProducts().get(0).getState());
    }


    @Test
    public void getInstitutionProducts_fullyNull() {
        // given and when
        Products response = restClient.getInstitutionProducts(testCase2instIdMap.get(TestCase.FULLY_NULL),null);
        // then
        Assert.assertNotNull(response);
        Assert.assertNull(response.getProducts());
    }


    @Test
    public void getInstitutionProducts_emptyResult() {
        //given
        EnumSet<ProductState> states = EnumSet.of(ProductState.ACTIVE, ProductState.PENDING);
        // given and when
        Products response = restClient.getInstitutionProducts(testCase2instIdMap.get(TestCase.EMPTY_RESULT), states);
        // then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getProducts());
        Assert.assertTrue(response.getProducts().isEmpty());
    }


    @Test
    public void getOnBoardingInfo_fullyValued() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_VALUED), null);
        // then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPerson());
        Assert.assertNotNull(response.getInstitutions());
        Assert.assertNotNull(response.getPerson().getName());
        Assert.assertNotNull(response.getPerson().getSurname());
        Assert.assertNotNull(response.getPerson().getTaxCode());
        Assert.assertNotNull(response.getInstitutions().get(0).getInstitutionId());
        Assert.assertNotNull(response.getInstitutions().get(0).getDescription());
        Assert.assertNotNull(response.getInstitutions().get(0).getTaxCode());
        Assert.assertNotNull(response.getInstitutions().get(0).getDigitalAddress());
        Assert.assertNotNull(response.getInstitutions().get(0).getState());
        Assert.assertNotNull(response.getInstitutions().get(0).getRole());
        Assert.assertNotNull(response.getInstitutions().get(0).getAttributes());
        Assert.assertNotNull(response.getInstitutions().get(0).getProductInfo());
        Assert.assertNotNull(response.getInstitutions().get(0).getProductInfo().getId());
        Assert.assertNotNull(response.getInstitutions().get(0).getProductInfo().getRole());
        Assert.assertNotNull(response.getInstitutions().get(0).getProductInfo().getCreatedAt());
    }


    @Test
    public void getOnBoardingInfo_fullyNull() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_NULL), EnumSet.of(ACTIVE));
        // then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPerson());
        Assert.assertNotNull(response.getInstitutions());
        Assert.assertNull(response.getPerson().getName());
        Assert.assertNull(response.getPerson().getSurname());
        Assert.assertNull(response.getPerson().getTaxCode());
        Assert.assertNull(response.getInstitutions().get(0).getInstitutionId());
        Assert.assertNull(response.getInstitutions().get(0).getDescription());
        Assert.assertNull(response.getInstitutions().get(0).getTaxCode());
        Assert.assertNull(response.getInstitutions().get(0).getDigitalAddress());
        Assert.assertNull(response.getInstitutions().get(0).getState());
        Assert.assertNull(response.getInstitutions().get(0).getRole());
        Assert.assertNull(response.getInstitutions().get(0).getAttributes());
        Assert.assertNull(response.getInstitutions().get(0).getProductInfo());
    }


    @Test
    public void getOnBoardingInfo_emptyResult() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.EMPTY_RESULT), EnumSet.of(ACTIVE, PENDING));
        // then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getInstitutions().isEmpty());
        Assert.assertNull(response.getPerson());
    }


    @Test
    public void onboardingSubdelegates() {
        // given
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId("institutionId");
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingSubdelegates(onboardingRequest);
        // then
        Assertions.assertDoesNotThrow(executable);
    }


    @Test
    public void onboardingOperators() {
        // given
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId("institutionId");
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingOperators(onboardingRequest);
        // then
        Assertions.assertDoesNotThrow(executable);
    }


    @Test
    public void suspendRelationship() {
        // given
        String relationshipId = "relationshipId";
        // when
        Executable executable = () -> restClient.suspendRelationship(relationshipId);
        // then
        Assertions.assertDoesNotThrow(executable);
    }


    @Test
    public void activateRelationship() {
        // given
        String relationshipId = "relationshipId";
        // when
        Executable executable = () -> restClient.activateRelationship(relationshipId);
        // then
        Assertions.assertDoesNotThrow(executable);
    }

}