package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.rest.config.PartyProcessRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.InstitutionPut;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.User;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import lombok.SneakyThrows;
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

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.PENDING;
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
        response.forEach(relationshipInfo -> {
            TestUtils.checkNotNullFields(relationshipInfo);
            TestUtils.checkNotNullFields(relationshipInfo.getInstitutionUpdate());
            TestUtils.checkNotNullFields(relationshipInfo.getBilling());
        });
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
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_VALUED), null, null);
        // then
        assertNotNull(response);
        assertNotNull(response.getInstitutions());
        assertFalse(response.getInstitutions().isEmpty());
        response.getInstitutions().forEach(onboardingData -> {
            TestUtils.checkNotNullFields(onboardingData, "subunitCode", "subunitType", "aooParentCode", "parentDescription", "rootParentId");
            assertNotNull(onboardingData.getAttributes());
            assertFalse(onboardingData.getAttributes().isEmpty());
            onboardingData.getAttributes().forEach(attribute -> {
                assertNotNull(attribute);
                TestUtils.checkNotNullFields(attribute);
            });
            TestUtils.checkNotNullFields(onboardingData.getProductInfo());
            TestUtils.checkNotNullFields(onboardingData.getBilling());
        });

    }


    @Test
    void getOnBoardingInfo_fullyNull() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_NULL), null, EnumSet.of(ACTIVE));
        // then
        assertNotNull(response);
        assertNull(response.getUserId());
        assertNull(response.getInstitutions());
    }


    @Test
    void getOnBoardingInfo_emptyResult() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.EMPTY_RESULT), null, EnumSet.of(ACTIVE, PENDING));
        // then
        assertNotNull(response);
        assertTrue(response.getInstitutions().isEmpty());
        assertNull(response.getUserId());
    }


    @Test
    void putInstitution() {
        // given
        final String institutionIdMock = "institutionId";
        final String institutionPutMockCode = "setCode";
        InstitutionPut geographicTaxonomyCodesMock = TestUtils.mockInstance(new InstitutionPut());
        geographicTaxonomyCodesMock.setGeographicTaxonomyCodes(List.of(TestUtils.mockInstance(institutionPutMockCode)));

        // when
        Executable executable = () -> restClient.updateInstitutionGeographicTaxonomy(institutionIdMock, geographicTaxonomyCodesMock);

        // then
        assertDoesNotThrow(executable);

    }

    @Test
    void onboardingSubdelegates() {
        // given
        OnboardingUsersRequest onboardingUsersRequest = new OnboardingUsersRequest();
        onboardingUsersRequest.setInstitutionId("institutionId");
        onboardingUsersRequest.setProductId("productId");
        onboardingUsersRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingSubdelegates(onboardingUsersRequest);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void onboardingOperators() {
        // given
        OnboardingUsersRequest onboardingUsersRequest = new OnboardingUsersRequest();
        onboardingUsersRequest.setInstitutionId("institutionId");
        onboardingUsersRequest.setProductId("productId");
        onboardingUsersRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingOperators(onboardingUsersRequest);
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
        TestUtils.checkNotNullFields(response, "city","country","county");
        response.getAttributes().forEach(TestUtils::checkNotNullFields);

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
        assertNull(response.getExternalId());
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
        TestUtils.checkNotNullFields(response, "city","county","country");
        response.getAttributes().forEach(TestUtils::checkNotNullFields);
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
        assertNull(response.getExternalId());
        assertNull(response.getTaxCode());
        assertNull(response.getZipCode());
    }

    @Test
    void approveOnboardingRequest() {
        // given
        String tokenId = "tokenId";
        // when
        Executable executable = () -> restClient.approveOnboardingRequest(tokenId);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void rejectOnboardingRequest() {
        // given
        String tokenId = "tokenId";
        // when
        Executable executable = () -> restClient.rejectOnboardingRequest(tokenId);
        // then
        assertDoesNotThrow(executable);
    }


}