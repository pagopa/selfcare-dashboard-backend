package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.rest.config.PartyProcessRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.User;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.*;

import static it.pagopa.selfcare.dashboard.connector.model.PartyRole.MANAGER;
import static it.pagopa.selfcare.dashboard.connector.model.PartyRole.OPERATOR;
import static it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState.PENDING;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(
        locations = "classpath:config/party-process-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = PartyProcessRestClientTest.RandomPortInitializer.class,
        classes = {PartyProcessRestClientTestConfig.class, HttpClientConfiguration.class})
@Disabled
class PartyProcessRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/party-process")
//                    .notifier(new ConsoleNotifier(false))
//                    .gzipDisabled(true)
//                    .disableRequestJournal()
//                    .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
            )
//            .configureStaticDsl(true)
            .build();


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_PROCESS_URL=%s/pdnd-interop-uservice-party-process/0.0.1",
                            wm.getRuntimeInfo().getHttpBaseUrl())
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
    void getInstitutionRelationships_fullyValued() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        EnumSet<PartyRole> roles = null;
        EnumSet<RelationshipState> states = null;
        Set<String> products = null;
        Set<String> productRole = null;
        String userId = null;
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(externalId, roles, states, products, productRole, userId);
        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertNotNull(response.get(0).getId());
        assertNotNull(response.get(0).getFrom());
        assertNotNull(response.get(0).getTo());
        assertNotNull(response.get(0).getName());
        assertNotNull(response.get(0).getSurname());
        assertNotNull(response.get(0).getEmail());
        assertNotNull(response.get(0).getRole());
        assertNotNull(response.get(0).getState());
        assertNotNull(response.get(0).getCreatedAt());
        assertNotNull(response.get(0).getUpdatedAt());
        assertNotNull(response.get(0).getProduct());
        assertNotNull(response.get(0).getProduct().getId());
        assertNotNull(response.get(0).getProduct().getRole());
        assertNotNull(response.get(0).getProduct().getCreatedAt());
    }


    @Test
    void getInstitutionRelationships_fullyNull() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_NULL);
        EnumSet<PartyRole> roles = null;
        EnumSet<RelationshipState> states = null;
        Set<String> products = null;
        Set<String> productRole = null;
        String userId = null;
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(externalId, roles, states, products, productRole, userId);
        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertNull(response.get(0).getId());
        assertNull(response.get(0).getFrom());
        assertNull(response.get(0).getRole());
        assertNull(response.get(0).getProduct());
        assertNull(response.get(0).getState());
    }


    @Test
    void getInstitutionRelationships_emptyResult() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.EMPTY_RESULT);
        EnumSet<PartyRole> roles = EnumSet.of(MANAGER, OPERATOR);
        EnumSet<RelationshipState> states = EnumSet.of(ACTIVE, PENDING);
        Set<String> products = Set.of("prod1", "prod2");
        Set<String> productRole = Set.of("api", "security");
        String userId = "userId";
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(externalId, roles, states, products, productRole, userId);

        // then
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }


    @Test
    void getInstitutionProducts_fullyValued() {
        // given and when
        Products response = restClient.getInstitutionProducts(testCase2instIdMap.get(TestCase.FULLY_VALUED), null);
        // then
        assertNotNull(response);
        assertNotNull(response.getProducts());
        assertFalse(response.getProducts().isEmpty());
        assertNotNull(response.getProducts().get(0).getId());
        assertNotNull(response.getProducts().get(0).getState());
    }


    @Test
    void getInstitutionProducts_fullyNull() {
        // given and when
        Products response = restClient.getInstitutionProducts(testCase2instIdMap.get(TestCase.FULLY_NULL), null);
        // then
        assertNotNull(response);
        assertNull(response.getProducts());
    }


    @Test
    void getInstitutionProducts_emptyResult() {
        //given
        EnumSet<ProductState> states = EnumSet.of(ProductState.ACTIVE, ProductState.PENDING);
        // given and when
        Products response = restClient.getInstitutionProducts(testCase2instIdMap.get(TestCase.EMPTY_RESULT), states);
        // then
        assertNotNull(response);
        assertNotNull(response.getProducts());
        assertTrue(response.getProducts().isEmpty());
    }


    @Test
    void getOnBoardingInfo_fullyValued() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_VALUED), null);
        // then
        assertNotNull(response);
        assertNotNull(response.getPerson());
        assertNotNull(response.getInstitutions());
        assertNotNull(response.getPerson().getName());
        assertNotNull(response.getPerson().getSurname());
        assertNotNull(response.getPerson().getTaxCode());
        assertNotNull(response.getInstitutions().get(0).getInstitutionId());
        assertNotNull(response.getInstitutions().get(0).getDescription());
        assertNotNull(response.getInstitutions().get(0).getTaxCode());
        assertNotNull(response.getInstitutions().get(0).getDigitalAddress());
        assertNotNull(response.getInstitutions().get(0).getState());
        assertNotNull(response.getInstitutions().get(0).getRole());
        assertNotNull(response.getInstitutions().get(0).getAttributes());
        assertNotNull(response.getInstitutions().get(0).getProductInfo());
        assertNotNull(response.getInstitutions().get(0).getProductInfo().getId());
        assertNotNull(response.getInstitutions().get(0).getProductInfo().getRole());
        assertNotNull(response.getInstitutions().get(0).getProductInfo().getCreatedAt());
    }


    @Test
    void getOnBoardingInfo_fullyNull() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_NULL), EnumSet.of(ACTIVE));
        // then
        assertNotNull(response);
        assertNotNull(response.getPerson());
        assertNotNull(response.getInstitutions());
        assertNull(response.getPerson().getName());
        assertNull(response.getPerson().getSurname());
        assertNull(response.getPerson().getTaxCode());
        assertNull(response.getInstitutions().get(0).getInstitutionId());
        assertNull(response.getInstitutions().get(0).getDescription());
        assertNull(response.getInstitutions().get(0).getTaxCode());
        assertNull(response.getInstitutions().get(0).getDigitalAddress());
        assertNull(response.getInstitutions().get(0).getState());
        assertNull(response.getInstitutions().get(0).getRole());
        assertNull(response.getInstitutions().get(0).getAttributes());
        assertNull(response.getInstitutions().get(0).getProductInfo());
    }


    @Test
    void getOnBoardingInfo_emptyResult() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.EMPTY_RESULT), EnumSet.of(ACTIVE, PENDING));
        // then
        assertNotNull(response);
        assertTrue(response.getInstitutions().isEmpty());
        assertNull(response.getPerson());
    }


    @Test
    void onboardingSubdelegates() {
        // given
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId("institutionId");
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingSubdelegates(onboardingRequest);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void onboardingOperators() {
        // given
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId("institutionId");
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingOperators(onboardingRequest);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void suspendRelationship() {
        // given
        String relationshipId = "relationshipId";
        // when
        Executable executable = () -> restClient.suspendRelationship(relationshipId);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void activateRelationship() {
        // given
        String relationshipId = "relationshipId";
        // when
        Executable executable = () -> restClient.activateRelationship(relationshipId);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void deleteRelationship() {
        // given
        String relationshipId = "relationshipId";
        // when
        Executable executable = () -> restClient.deleteRelationshipById(relationshipId);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void getInstitution_fullyValued() {
        // given
        String id = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        // when
        Institution response = restClient.getInstitution(id);
        assertNotNull(response);
        assertNotNull(response.getAddress());
        assertNotNull(response.getDescription());
        assertNotNull(response.getDigitalAddress());
        assertNotNull(response.getId());
        assertNotNull(response.getInstitutionId());
        assertNotNull(response.getTaxCode());
        assertNotNull(response.getZipCode());
    }


    @Test
    void getInstitution_fullyNull() {
        // given
        String id = testCase2instIdMap.get(TestCase.FULLY_NULL);
        // when
        Institution response = restClient.getInstitution(id);
        assertNotNull(response);
        assertNull(response.getAddress());
        assertNull(response.getDescription());
        assertNull(response.getDigitalAddress());
        assertNull(response.getId());
        assertNull(response.getInstitutionId());
        assertNull(response.getTaxCode());
        assertNull(response.getZipCode());
    }


    @Test
    void getInstitutionByExternalId_fullyValued() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        // when
        Institution response = restClient.getInstitutionByExternalId(externalId);
        assertNotNull(response);
        assertNotNull(response.getAddress());
        assertNotNull(response.getDescription());
        assertNotNull(response.getDigitalAddress());
        assertNotNull(response.getId());
        assertNotNull(response.getInstitutionId());
        assertNotNull(response.getTaxCode());
        assertNotNull(response.getZipCode());
    }


    @Test
    void getInstitutionByExternalId_fullyNull() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_NULL);
        // when
        Institution response = restClient.getInstitutionByExternalId(externalId);
        assertNotNull(response);
        assertNull(response.getAddress());
        assertNull(response.getDescription());
        assertNull(response.getDigitalAddress());
        assertNull(response.getId());
        assertNull(response.getInstitutionId());
        assertNull(response.getTaxCode());
        assertNull(response.getZipCode());
    }

}