package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.*;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.*;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.DelegationRestClientMapperImpl;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

import javax.validation.ValidationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.commons.utils.TestUtils.*;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static it.pagopa.selfcare.dashboard.connector.rest.CoreConnectorImpl.REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE;
import static it.pagopa.selfcare.dashboard.connector.rest.CoreConnectorImpl.REQUIRED_INSTITUTION_ID_MESSAGE;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                CoreConnectorImpl.class, InstitutionMapperImpl.class, DelegationRestClientMapperImpl.class
        }
)
class CoreConnectorImplTest {

    private final ObjectMapper mapper;

    @Autowired
    private CoreConnectorImpl msCoreConnector;

    @MockBean
    private CoreUserApiRestClient coreUserApiRestClientMock;

    @MockBean
    private CoreDelegationApiRestClient coreDelegationApiRestClient;

    @MockBean
    private CoreInstitutionApiRestClient coreInstitutionApiRestClient;

    @MockBean
    private CoreOnboardingApiRestClient coreOnboardingApiRestClient;

    @MockBean
    private CoreManagementApiRestClient coreManagementApiRestClient;
    @MockBean
    private BrokerMapper brokerMapper;

    @Captor
    private ArgumentCaptor<OnboardingInstitutionOperatorsRequest> onboardingRequestCaptor;

    public CoreConnectorImplTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());
    }

    private static final Function<it.pagopa.selfcare.commons.base.security.PartyRole, SelfCareAuthority> PARTY_2_SELC_ROLE = partyRole -> switch (partyRole) {
        case MANAGER, DELEGATE, SUB_DELEGATE -> ADMIN;
        default -> LIMITED;
    };


    @Test
    void getUserProducts_shouldGetEmptyData() {
        // given
        String userId = "userId";

        UserProductsResponse userProductsResponse = new UserProductsResponse();
        userProductsResponse.setId(userId);
        userProductsResponse.setBindings(List.of());

        ResponseEntity<UserProductsResponse> userProductsResponseResponseEntity = mock(ResponseEntity.class);
        when(userProductsResponseResponseEntity.getBody()).thenReturn(userProductsResponse);

        when(coreUserApiRestClientMock._getUserProductsInfoUsingGET(any(), any(), any()))
                .thenReturn(userProductsResponseResponseEntity);
        // when
        List<InstitutionInfo> institutions = msCoreConnector.getUserProducts(userId);
        // then
        assertNotNull(institutions);
        assertEquals(0, institutions.size());

        verify(coreUserApiRestClientMock, times(1))
                ._getUserProductsInfoUsingGET(eq(userId), isNull(), eq(ACTIVE.name()  +","+ PENDING.name() +","+ TOBEVALIDATED.name()));
        verifyNoMoreInteractions(coreUserApiRestClientMock);
    }

    @Test
    void getUserProducts_shouldGetData() {
        // given
        String userId = "userId";

        UserProductsResponse userProductsResponse = new UserProductsResponse();
        userProductsResponse.setId(userId);
        InstitutionProducts institutionProducts = new InstitutionProducts();
        institutionProducts.setInstitutionId("institutionId");
        institutionProducts.setProducts(List.of(it.pagopa.selfcare.core.generated.openapi.v1.dto.Product.builder()
                .status(it.pagopa.selfcare.core.generated.openapi.v1.dto.Product.StatusEnum.ACTIVE)
                .status(it.pagopa.selfcare.core.generated.openapi.v1.dto.Product.StatusEnum.ACTIVE)
                .build()));
        userProductsResponse.setBindings(List.of(institutionProducts));

        ResponseEntity<UserProductsResponse> userProductsResponseResponseEntity = mock(ResponseEntity.class);
        when(userProductsResponseResponseEntity.getBody()).thenReturn(userProductsResponse);
        when(coreUserApiRestClientMock._getUserProductsInfoUsingGET(any(), any(), any()))
                .thenReturn(userProductsResponseResponseEntity);
        // when
        List<InstitutionInfo> institutions = msCoreConnector.getUserProducts(userId);

        assertEquals(userProductsResponse.getBindings().get(0).getInstitutionRootName(), institutions.get(0).getParentDescription());
        assertEquals(userProductsResponse.getBindings().get(0).getInstitutionId(), institutions.get(0).getId());

        verify(coreUserApiRestClientMock, times(1))
                ._getUserProductsInfoUsingGET(eq(userId), isNull(), eq(ACTIVE.name() +","+ PENDING.name() +","+ TOBEVALIDATED.name()));
        verifyNoMoreInteractions(coreUserApiRestClientMock);
    }

    @Test
    void getInstitution_nullInstitutionId() {
        // given
        String institutionId = null;
        // when
        Executable executable = () -> msCoreConnector.getInstitution(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(coreInstitutionApiRestClient);
    }


    @Test
    void getInstitution_nullResponse() {
        // given
        String institutionId = "institutionId";
        InstitutionResponse instResp = mockInstance(new InstitutionResponse());
        // when
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId)).thenReturn(ResponseEntity.ok().build());
        Institution institution = msCoreConnector.getInstitution(institutionId);
        // then
        assertNull(institution);
        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionByIdUsingGET(institutionId);
        verifyNoMoreInteractions(coreInstitutionApiRestClient);

    }


    @Test
    void getInstitution() {
        // given
        String institutionId = "institutionId";
        InstitutionResponse institutionMock = mockInstance(new InstitutionResponse());
        AttributesResponse attribute = mockInstance(new AttributesResponse());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeoTaxonomies())));
        institutionMock.setAttributes(List.of(attribute));
        institutionMock.setOnboarding(Collections.emptyList());
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(any()))
                .thenReturn(ResponseEntity.of(Optional.of(institutionMock)));
        // when
        Institution institution = msCoreConnector.getInstitution(institutionId);
        // then
        assertSame(institutionMock.getDescription(), institution.getDescription());
        checkNotNullFields(institution, "supportContact", "billing", "paymentServiceProvider", "dataProtectionOfficer", "city", "country", "county", "additionalInformations");
        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionByIdUsingGET(institutionId);
        verifyNoMoreInteractions(coreInstitutionApiRestClient);

    }

    @Test
    void updateInstitutionDescription() {
        // given
        String institutionId = "setId";
        UpdateInstitutionResource resource = mockInstance(new UpdateInstitutionResource());
        InstitutionPut resourcePut = mockInstance(new InstitutionPut());
        InstitutionResponse institutionMock = mockInstance(new InstitutionResponse());
        when(coreInstitutionApiRestClient._updateInstitutionUsingPUT(anyString(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(institutionMock)));
        // when
        Institution institution = msCoreConnector.updateInstitutionDescription(institutionId, resource);
        // then
        assertEquals(institution.getId(), institutionId);
        assertEquals(institution.getDescription(), resource.getDescription());
        assertEquals(institution.getDigitalAddress(), resource.getDigitalAddress());
        verify(coreInstitutionApiRestClient, times(1))
                ._updateInstitutionUsingPUT(institutionId, resourcePut);
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void createDelegation() {
        // given
        DelegationRequest delegation = new DelegationRequest();
        delegation.setId("id");
        DelegationResponse delegationId = new DelegationResponse();
        delegationId.setId("id");
        when(coreDelegationApiRestClient._createDelegationUsingPOST(any()))
                .thenReturn(ResponseEntity.of(Optional.of(delegationId)));
        DelegationId response = msCoreConnector.createDelegation(delegation);
        assertNotNull(response);
        assertEquals(response.getId(), delegationId.getId());
    }

    @Test
    void findInstitutionsByProductIdAndType() {
        // given
        final String productId = "prod";
        final String type = "PT";
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setCode("taxCode");
        brokerInfo.setDescription("description");
        BrokerResponse brokerResponse = new BrokerResponse();
        brokerResponse.setDescription("description");
        brokerResponse.setTaxCode("taxCode");
        when(brokerMapper.fromInstitutions(anyList())).thenReturn(List.of(brokerInfo));
        when(coreInstitutionApiRestClient._getInstitutionBrokersUsingGET(any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(List.of(brokerResponse))));
        // when
        List<BrokerInfo> response = msCoreConnector.findInstitutionsByProductAndType(productId, type);
        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertNotNull(response.get(0));
        assertEquals(response.get(0).getCode(), brokerInfo.getCode());
        assertEquals(response.get(0).getDescription(), brokerResponse.getDescription());

    }

    @Test
    void getDelegationUsingFrom_shouldGetData() {
        // given
        DelegationResponse delegationResponse = dummyDelegationResponse();
        List<DelegationResponse> delegationResponseList = new ArrayList<>();
        delegationResponseList.add(delegationResponse);
        ResponseEntity<List<DelegationResponse>> delegationResponseEntity = new ResponseEntity<>(delegationResponseList, null, HttpStatus.OK);
        GetDelegationParameters parameters = dummyDelegationParameters();

        when(coreDelegationApiRestClient._getDelegationsUsingGET(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(delegationResponseEntity);


        // when
        List<Delegation> delegationList = msCoreConnector.getDelegations(dummyDelegationParameters());
        // then
        assertNotNull(delegationList);
        assertEquals(1, delegationList.size());

        assertEquals(delegationResponseList.get(0).getId(), delegationList.get(0).getId());
        assertEquals(delegationResponseList.get(0).getInstitutionId(), delegationList.get(0).getInstitutionId());
        assertEquals(delegationResponseList.get(0).getBrokerId(), delegationList.get(0).getBrokerId());
        assertEquals(delegationResponseList.get(0).getProductId(), delegationList.get(0).getProductId());
        assertEquals(delegationResponseList.get(0).getType().toString(), delegationList.get(0).getType().toString());
        assertEquals(delegationResponseList.get(0).getInstitutionName(), delegationList.get(0).getInstitutionName());
        assertEquals(delegationResponseList.get(0).getBrokerName(), delegationList.get(0).getBrokerName());

        verify(coreDelegationApiRestClient, times(1))
                ._getDelegationsUsingGET(parameters.getFrom(), parameters.getTo(), parameters.getProductId(), parameters.getSearch(), parameters.getTaxCode(), parameters.getMode(), parameters.getOrder(), parameters.getPage(), parameters.getSize());
        verifyNoMoreInteractions(coreDelegationApiRestClient);
    }

    @Test
    void getDelegationUsingFrom_shouldGetEmptyData() {
        // given
        ResponseEntity<List<DelegationResponse>> delegationResponseEntity = mock(ResponseEntity.class);
        GetDelegationParameters parameters = dummyDelegationParameters();

        when(delegationResponseEntity.getBody()).thenReturn(null);

        when(coreDelegationApiRestClient._getDelegationsUsingGET(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(delegationResponseEntity);


        // when
        List<Delegation> delegationList = msCoreConnector.getDelegations(dummyDelegationParameters());
        // then
        assertNotNull(delegationList);
        assertEquals(0, delegationList.size());

        verify(coreDelegationApiRestClient, times(1))
                ._getDelegationsUsingGET(parameters.getFrom(), parameters.getTo(), parameters.getProductId(), parameters.getSearch(), parameters.getTaxCode(), parameters.getMode(), parameters.getOrder(), parameters.getPage(), parameters.getSize());
        verifyNoMoreInteractions(coreDelegationApiRestClient);
    }


    private DelegationResponse dummyDelegationResponse() {
        DelegationResponse delegationResponse = new DelegationResponse();
        delegationResponse.setInstitutionId("from");
        delegationResponse.setBrokerId("to");
        delegationResponse.setId("setId");
        delegationResponse.setProductId("setProductId");
        delegationResponse.setType(DelegationResponse.TypeEnum.PT);
        delegationResponse.setInstitutionName("setInstitutionFromName");
        delegationResponse.setBrokerName("brokerName");
        return delegationResponse;
    }

    @Test
    void updateUser() {
        //given
        String userId = UUID.randomUUID().toString();
        String institutionId = UUID.randomUUID().toString();
        //when
        Executable executable = () -> msCoreConnector.updateUser(userId, institutionId);
        //then
        assertDoesNotThrow(executable);
        verify(coreUserApiRestClientMock, times(1))._updateUserUsingPOST(userId, institutionId);
    }

    @Test
    void getInstitution_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(institutionId, null, ACTIVE.name()))
                .thenReturn(ResponseEntity.of(Optional.of(new OnboardingInfoResponse())));
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }


    @Test
    void getInstitution_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(onBoardingInfo)));
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void getInstitution_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(onBoardingInfo)));
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }


    @Test
    void getInstitution_nullAttributes() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        OnboardedInstitutionResponse onboardingData = mockInstance(new OnboardedInstitutionResponse());
        onboardingData.setState("PENDING");
        onboardingData.setGeographicTaxonomies(List.of(mockInstance(new GeoTaxonomies())));
        onBoardingInfo.setInstitutions(singletonList(onboardingData));
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(onBoardingInfo)));
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void getInstitution_emptyAttributes() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        OnboardedInstitutionResponse onboardingData = mockInstance(new OnboardedInstitutionResponse());
        onboardingData.setState("SUSPENDED");
        onboardingData.setGeographicTaxonomies(List.of(mockInstance(new GeoTaxonomies())));
        onBoardingInfo.setInstitutions(singletonList(onboardingData));
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(onBoardingInfo)));
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void getInstitution_nullGeographicTaxonomy() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        OnboardedInstitutionResponse onboardingData = mockInstance(new OnboardedInstitutionResponse(), "setGeographicTaxonomies");
        onboardingData.setState("PENDING");
        onBoardingInfo.setInstitutions(singletonList(onboardingData));
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(onBoardingInfo)));
        // when

        // then
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        assertNotNull(institutionInfo.getGeographicTaxonomies());
        assertTrue(institutionInfo.getGeographicTaxonomies().isEmpty());

        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void getOnBoardedInstitution() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        OnboardedInstitutionResponse onboardingData = mockInstance(new OnboardedInstitutionResponse());
        onboardingData.setAttributes(List.of(mockInstance(new AttributesResponse())));
        onboardingData.setGeographicTaxonomies(List.of(mockInstance(new GeoTaxonomies())));
        onboardingData.setState("ACTIVE");
        onBoardingInfo.setInstitutions(singletonList(onboardingData));
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(onBoardingInfo)));
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        checkNotNullFields(institutionInfo, "paymentServiceProvider", "dataProtectionOfficer", "city", "country", "county", "additionalInformations");
        assertEquals(onboardingData.getDescription(), institutionInfo.getDescription());
        assertEquals(onboardingData.getDigitalAddress(), institutionInfo.getDigitalAddress());
        assertEquals(onboardingData.getExternalId(), institutionInfo.getExternalId());
        assertEquals(onboardingData.getState(), institutionInfo.getStatus().name());
        assertEquals(onboardingData.getAttributes().get(0).getCode(), institutionInfo.getCategory());
        assertEquals(onboardingData.getGeographicTaxonomies().get(0).getCode(), institutionInfo.getGeographicTaxonomies().get(0).getCode());
        assertEquals(onboardingData.getGeographicTaxonomies().get(0).getDesc(), institutionInfo.getGeographicTaxonomies().get(0).getDesc());
        assertEquals(onboardingData.getZipCode(), institutionInfo.getZipCode());
        reflectionEqualsByName(onboardingData.getPaymentServiceProvider(), institutionInfo.getPaymentServiceProvider());
        reflectionEqualsByName(onboardingData.getSupportContact(), institutionInfo.getSupportContact());
        reflectionEqualsByName(onboardingData.getBilling(), institutionInfo.getBilling());
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void updateGeographicTaxonomy() {
        // given
        String institutionId = "institutionId";
        GeographicTaxonomyList geographicTaxonomiesMock = new GeographicTaxonomyList();
        geographicTaxonomiesMock.setGeographicTaxonomyList(List.of(mockInstance(new GeographicTaxonomy())));
        System.out.println(geographicTaxonomiesMock);
        when(coreInstitutionApiRestClient._updateInstitutionUsingPUT(anyString(), any())).thenReturn(ResponseEntity.ok().build());
        // when
        msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomiesMock);
        // then
        ArgumentCaptor<InstitutionPut> argumentCaptor = ArgumentCaptor.forClass(InstitutionPut.class);
        verify(coreInstitutionApiRestClient, times(1))
                ._updateInstitutionUsingPUT(Mockito.eq(institutionId), argumentCaptor.capture());
        InstitutionPut institutionPut = argumentCaptor.getValue();
        assertEquals(geographicTaxonomiesMock.getGeographicTaxonomyList().get(0).getCode(), institutionPut.getGeographicTaxonomyCodes().get(0));
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void updateGeographicTaxonomy_hasNullInstitutionId() {
        // given
        String institutionId = null;
        GeographicTaxonomyList geographicTaxonomiesMock = new GeographicTaxonomyList();
        // when
        Executable executable = () -> msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomiesMock);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void updateGeographicTaxonomy_hasNullGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        GeographicTaxonomyList geographicTaxonomiesMock = null;
        // when
        Executable executable = () -> msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomiesMock);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE, e.getMessage());
        verifyNoInteractions(coreInstitutionApiRestClient);
    }


    @Test
    void getInstitutionProducts_nullProducts() {
        // given
        String institutionId = "institutionId";
        // when
        when(coreInstitutionApiRestClient._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() +"," +ProductState.PENDING.name()))
                .thenReturn(ResponseEntity.ok(null));
        List<PartyProduct> institutionProducts = msCoreConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() +"," +ProductState.PENDING.name());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void getInstitutionProducts_nullProductsInfo() {
        // given
        String institutionId = "institutionId";
        when(coreInstitutionApiRestClient._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() +"," +ProductState.PENDING.name()))
                .thenReturn(ResponseEntity.ok(new OnboardedProducts()));
        // when
        List<PartyProduct> institutionProducts = msCoreConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() +"," +ProductState.PENDING.name());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }


    @Test
    void getInstitutionProducts_emptyProductsInfo() {
        // given
        String institutionId = "institutionId";
        OnboardedProducts products = new OnboardedProducts();
        products.setProducts(Collections.emptyList());
        when(coreInstitutionApiRestClient._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() +"," +ProductState.PENDING.name()))
                .thenReturn(ResponseEntity.ok(new OnboardedProducts()));
        //
        // when
        List<PartyProduct> institutionProducts = msCoreConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() +"," +ProductState.PENDING.name());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }


    @Test
    void getInstitutionProducts() {
        // given
        String institutionId = "institutionId";
        OnboardedProducts products = new OnboardedProducts();
        products.setProducts(List.of(mockInstance(new InstitutionProduct())));
        when(coreInstitutionApiRestClient._retrieveInstitutionProductsUsingGET(any(), any()))
                .thenReturn(ResponseEntity.ok(products));
        // when
        List<PartyProduct> institutionProducts = msCoreConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertFalse(institutionProducts.isEmpty());
        assertEquals(products.getProducts().get(0).getId(), institutionProducts.get(0).getId());

        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() +"," +ProductState.PENDING.name());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }


    @Test
    void getAuthInfo_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(institutionId, null, ACTIVE.name()))
                .thenReturn(ResponseEntity.ok().build());
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify( coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null,ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }


    @Test
    void getAuthInfo_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.ok(onBoardingInfo));
        // when
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }


    @Test
    void getAuthInfo_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.ok(onBoardingInfo));
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void getAuthInfo_nullProductInfo() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        OnboardedInstitutionResponse onboardingData = mockInstance(new OnboardedInstitutionResponse(), "setProductInfo");
        onboardingData.setState(ACTIVE.name());
        onBoardingInfo.setInstitutions(List.of(onboardingData));
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(onBoardingInfo)));
        // when
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void getAuthInfo() {
        // given
        String institutionId = "institutionId";
        OnboardingInfoResponse onBoardingInfo = new OnboardingInfoResponse();
        OnboardedInstitutionResponse onboardingData1 = mockInstance(new OnboardedInstitutionResponse());
        onboardingData1.setState(ACTIVE.name());
        OnboardedInstitutionResponse onboardingData2 = mockInstance(new OnboardedInstitutionResponse());
        onboardingData2.setState(ACTIVE.name());
        OnboardedInstitutionResponse onboardingData3 = mockInstance(new OnboardedInstitutionResponse());
        onboardingData3.setId(onboardingData1.getId());
        onboardingData3.setState(ACTIVE.name());
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3));
        when(coreOnboardingApiRestClient._onboardingInfoUsingGET(any(), any(), any()))
                .thenReturn(ResponseEntity.ok(onBoardingInfo));
        // when
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertFalse(authInfos.isEmpty());
        assertEquals(1, authInfos.size());
        AuthInfo authInfo = authInfos.iterator().next();
        assertNotNull(authInfo.getProductRoles());
        assertEquals(3, authInfo.getProductRoles().size());
        authInfo.getProductRoles().forEach(productRole -> {
            if (productRole.getProductId().equals(onboardingData1.getProductInfo().getId())) {
                assertEquals(onboardingData1.getProductInfo().getRole(), productRole.getProductRole());
            } else if (productRole.getProductId().equals(onboardingData3.getProductInfo().getId())) {
                assertEquals(onboardingData3.getProductInfo().getRole(), productRole.getProductRole());
            } else {
                fail();
            }
        });
        verify(coreOnboardingApiRestClient, times(1))
                ._onboardingInfoUsingGET(institutionId, null, ACTIVE.name());
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @ParameterizedTest
    @EnumSource(value = it.pagopa.selfcare.commons.base.security.PartyRole.class)
    void party2SelcRoleMapping(it.pagopa.selfcare.commons.base.security.PartyRole partyRole) {
        // when
        SelfCareAuthority authority = partyRole.getSelfCareAuthority();
        // then
        assertEquals(PARTY_2_SELC_ROLE.apply(partyRole), authority);
    }

    @Test
    void getUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        // when
        Executable executable = () -> msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void getUsers_nullResponse_emptyRole_emptyProductIds_emptyProductRole_emptyUserId() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));

        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(institutionId, null, null, ACTIVE.name() +","+ SUSPENDED.name(), null, null))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(eq(institutionId), isNull(), isNull(), notNull(), isNull(), isNull());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void getUsers_nullResponse() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(institutionId, null, null, null, null, null))
                .thenReturn(ResponseEntity.ok().build());
        // when
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(eq(institutionId), isNull(), isNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void getUsers_notEmptyProductIds() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId("productId");
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));

        when(coreInstitutionApiRestClient
                ._getUserInstitutionRelationshipsUsingGET(institutionId, null, null, ACTIVE.name() +","+ SUSPENDED.name(), userInfoFilter.getProductId(), null))
                .thenReturn(ResponseEntity.ok().build());
        // when
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(eq(institutionId), isNull(), isNull(), notNull(), eq(userInfoFilter.getProductId()), isNull());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void getUsers_notEmptyProductRoles() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductRoles(List.of("api", "security"));
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(institutionId, null, null, ACTIVE.name() +","+ SUSPENDED.name(), null, String.join(",", userInfoFilter.getProductRoles())))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));
        // when
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(eq(institutionId), isNull(), isNull(), isNotNull(), isNull(), eq(String.join(",", userInfoFilter.getProductRoles())));
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @ParameterizedTest
    @EnumSource(value = SelfCareAuthority.class)
    void getUsers_notEmptyRole(SelfCareAuthority selfCareAuthority) {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setRole(selfCareAuthority);
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));
        List<String> partyRoles = new ArrayList<>();
        for (it.pagopa.selfcare.commons.base.security.PartyRole partyRole : it.pagopa.selfcare.commons.base.security.PartyRole.values()) {
            if (userInfoFilter.getRole().equals(PARTY_2_SELC_ROLE.apply(partyRole))) {
                partyRoles.add(partyRole.name());
            }
        }

        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(eq(institutionId), isNull(), isNotNull(), isNotNull(), isNull(), isNull()))
                .thenReturn(ResponseEntity.of(Optional.of(new ArrayList<>())));
        // when
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(eq(institutionId), isNull(), isNotNull(), isNotNull(), isNull(), isNull());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void getUsers() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));

        List<RelationshipResult> relationshipsResponse = new ArrayList<>();
        RelationshipResult relationshipInfo1 = mockInstance(new RelationshipResult(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipResult relationshipInfo2 = mockInstance(new RelationshipResult(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(relationshipsResponse));
        // when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(userInfos);
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        ProductInfo product = productInfoMap.get(prodId);
        assertEquals(id, userInfo.getId());
        assertNotNull(product.getRoleInfos());
        assertNotNull(product.getId());
        assertNull(product.getTitle());
        assertNull(userInfo.getUser());
        assertNotNull(userInfo.getStatus());
        assertNotNull(userInfo.getRole());
        assertEquals(1, userInfo.getProducts().size());

        assertNotNull(productInfoMap.keySet());
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(eq(institutionId), any(), isNull(), notNull(), isNull(), isNull());
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void getUser() {
        // given
        String relationshipId = "relationshipId";

        RelationshipResult relationshipInfo1 = mockInstance(new RelationshipResult(), "setFrom");
        relationshipInfo1.setFrom(relationshipId);
        when(coreUserApiRestClientMock._getRelationshipUsingGET(anyString()))
                .thenReturn(ResponseEntity.ok(relationshipInfo1));
        // when
        UserInfo userInfo = msCoreConnector.getUser(relationshipId);
        // then
        assertNotNull(userInfo);
        assertEquals(relationshipId, userInfo.getId());
        assertNull(userInfo.getUser());
        assertNotNull(userInfo.getStatus());
        assertNotNull(userInfo.getRole());
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        assertEquals(1, userInfo.getProducts().size());
        ProductInfo product = productInfoMap.get(prodId);
        assertNotNull(product.getRoleInfos());
        assertNotNull(product.getId());
        assertNull(product.getTitle());
        verify(coreUserApiRestClientMock, times(1))
                ._getRelationshipUsingGET(anyString());
        verifyNoMoreInteractions(coreUserApiRestClientMock);
    }

    @Test
    void relationship_info_to_user_info_function() throws IOException {
        // given
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/relationInfo-to-userInfo.json");
        RelationshipResult relationshipInfo = mapper.readValue(stub, RelationshipResult.class);
        // when
        UserInfo userInfo = CoreConnectorImpl.RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
        // then
        assertNull(userInfo.getUser());
        assertEquals(relationshipInfo.getState().toString(), userInfo.getStatus());
        assertEquals(relationshipInfo.getFrom(), userInfo.getId());
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        ProductInfo product = productInfoMap.get(prodId);
        assertEquals(relationshipInfo.getProduct().getId(), product.getId());
        assertEquals(1, product.getRoleInfos().size());
        RoleInfo roleInfo = product.getRoleInfos().get(0);
        assertEquals(relationshipInfo.getProduct().getRole(), product.getRoleInfos().get(0).getRole());
        assertEquals(relationshipInfo.getId(), roleInfo.getRelationshipId());
        assertEquals(ADMIN, roleInfo.getSelcRole());
    }

    @Test
    void getUser_mergeRoleInfos() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/multi-role.json");
        List<RelationshipResult> relationshipsResponse = mapper.readValue(stub, new TypeReference<>() {});
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn((ResponseEntity.ok(relationshipsResponse)));
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        //then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        assertEquals(2, productInfoMap.values().size());
        assertEquals(2, productInfoMap.get("prod-io").getRoleInfos().size());
        assertEquals(1, productInfoMap.get("prod-pn").getRoleInfos().size());

    }

    @Test
    void getUsers_higherRoleForActiveUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/higher-role-active.json");
        List<RelationshipResult> relationshipsResponse = mapper.readValue(stub, new TypeReference<>() {});
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn((ResponseEntity.ok(relationshipsResponse)));
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        //Then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(2, userInfo.getProducts().size());
    }

    @Test
    void getUser_getProductFromMerge() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();


        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/merge.json");
        List<RelationshipResult> relationshipsResponse = mapper.readValue(stub, new TypeReference<>() {});
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn((ResponseEntity.ok(relationshipsResponse)));
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        //then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        assertEquals(relationshipsResponse.size(), userInfo.getProducts().size());
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("PENDING", userInfo.getStatus());
    }

    @Test
    void getUsers_higherRoleForPendingUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();


        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/higher-role-pending.json");
        List<RelationshipResult> relationshipsResponse = mapper.readValue(stub, new TypeReference<>() {});
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn((ResponseEntity.ok(relationshipsResponse)));
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("PENDING", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/active-role-different-status.json");
        List<RelationshipResult> relationshipsResponse = mapper.readValue(stub, new TypeReference<>() {});
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn((ResponseEntity.ok(relationshipsResponse)));
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(LIMITED, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }


    @Test
    void getUsers_activeRoleUserDifferentStatus_2() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/active-role-different-status-2.json");
        List<RelationshipResult> relationshipsResponse = mapper.readValue(stub, new TypeReference<>() {});
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn((ResponseEntity.ok(relationshipsResponse)));
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());

    }

    @Test
    void getUsers_activeRoleUserDifferentStatus2() {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        List<RelationshipResult> relationshipsResponse = new ArrayList<>();
        RelationshipResult relationshipInfo1 = mockInstance(new RelationshipResult(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setRole(RelationshipResult.RoleEnum.valueOf(PartyRole.OPERATOR.name()));
        relationshipInfo1.setState(RelationshipResult.StateEnum.valueOf(PENDING.name()));
        RelationshipResult relationshipInfo2 = mockInstance(new RelationshipResult(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setRole(RelationshipResult.RoleEnum.valueOf(PartyRole.DELEGATE.name()));
        relationshipInfo2.setState(RelationshipResult.StateEnum.valueOf(ACTIVE.name()));
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(relationshipsResponse));
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());

    }

    @Test
    void userInfoFilter_emptyOptionals() {
        //given
        List<String> productRoles = null;
        String userId = null;
        SelfCareAuthority role = null;
        String productId = null;
        List<RelationshipState> allowedStates = null;
        //when
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setUserId(userId);
        filter.setProductRoles(productRoles);
        filter.setProductId(productId);
        filter.setRole(role);
        filter.setAllowedStates(allowedStates);
        //then
        assertNull(filter.getProductId());
        assertNull(filter.getProductRoles());
        assertNull(filter.getUserId());
        assertNull(filter.getRole());
        assertNull(filter.getAllowedStates());
    }

    @Test
    void createUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        String productId = "productId";
        String productTitle = "productTitle";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> msCoreConnector.createUsers(institutionId, productId, userId, createUserDto, productTitle);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void createUsers_nullProductId() {
        // given
        String institutionId = "institutionId";
        String productTitle = "productTitle";
        String productId = null;
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> msCoreConnector.createUsers(institutionId, productId, userId, createUserDto, productTitle);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Product id is required", e.getMessage());
        verifyNoInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void createUsers_nullUser() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productTitle = "productTitle";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = null;
        // when
        Executable executable = () -> msCoreConnector.createUsers(institutionId, productId, userId, createUserDto, productTitle);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User is required", e.getMessage());
        verifyNoInteractions(coreOnboardingApiRestClient);
    }

    @Test
    void createUsers_nullUserId() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productTitle = "productTitle";
        String userId = null;
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> msCoreConnector.createUsers(institutionId, productId, userId, createUserDto, productTitle);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An User Id is required", e.getMessage());
        verifyNoInteractions(coreOnboardingApiRestClient);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void createUsers(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productTitle = "productTitle";
        String productRoles = "Operator Api";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
        // when
        Executable executable = () -> msCoreConnector.createUsers(institutionId, productId, userId, createUserDto, productTitle);
        // then
        switch (partyRole) {
            case SUB_DELEGATE:
                assertDoesNotThrow(executable);
                verify(coreOnboardingApiRestClient, times(1))
                        ._onboardingInstitutionSubDelegateUsingPOST(onboardingRequestCaptor.capture());
                verifyRequest(institutionId, productId, createUserDto, onboardingRequestCaptor, userId);
                break;
            case OPERATOR:
                assertDoesNotThrow(executable);
                verify(coreOnboardingApiRestClient, times(1))
                        ._onboardingInstitutionOperatorsUsingPOST(onboardingRequestCaptor.capture());
                verifyRequest(institutionId, productId, createUserDto, onboardingRequestCaptor, userId);
                break;
            default:
                IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
                assertEquals("Invalid Party role", e.getMessage());
        }
        verifyNoMoreInteractions(coreOnboardingApiRestClient);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void checkExistingRelationshipRoles_noUserExisting(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator Api";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
        UserInfo.UserInfoFilter mockUserInfoFilter = new UserInfo.UserInfoFilter();
        mockUserInfoFilter.setProductId(productId);
        mockUserInfoFilter.setUserId(userId);
        mockUserInfoFilter.setAllowedStates(List.of(ACTIVE));
        List<RelationshipResult> mockRelationshipsResponse = getRelationshipResultList();
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(mockRelationshipsResponse));
        // when
        Assertions.assertThrows(ValidationException.class, () -> msCoreConnector.checkExistingRelationshipRoles(institutionId, productId, createUserDto, userId)
                , "User role conflict");
        // then
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(anyString(), any(), any(), any(), any(), any());
    }

    private static List<RelationshipResult> getRelationshipResultList() {
        RelationshipResult mockRelationshipResult = new RelationshipResult();
        mockRelationshipResult.setFrom("from");
        mockRelationshipResult.setId("id");
        mockRelationshipResult.setTo("to");
        it.pagopa.selfcare.core.generated.openapi.v1.dto.ProductInfo productInfo = new it.pagopa.selfcare.core.generated.openapi.v1.dto.ProductInfo();
        productInfo.setRole("MANAGER");
        mockRelationshipResult.setProduct(productInfo);
        List<RelationshipResult> mockRelationshipsResponse = new ArrayList<>();
        mockRelationshipsResponse.add(mockRelationshipResult);
        return mockRelationshipsResponse;
    }

    @Test
    void createUser_multiplePartyRole() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productTitle = "productTitle";
        String productRoles1 = "Operator Api";
        String productRoles2 = "Operator Security";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        PartyRole partyRole2 = PartyRole.SUB_DELEGATE;
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyROle");
        CreateUserDto.Role roleMock2 = mockInstance(new CreateUserDto.Role(), "setPartyROle");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        roleMock2.setProductRole(productRoles2);
        roleMock2.setPartyRole(partyRole2);
        createUserDto.setRoles(Set.of(roleMock1, roleMock2));
        // when
        Executable executable = () -> msCoreConnector.createUsers(institutionId, productId, userId, createUserDto, productTitle);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Is not allowed to create both SUB_DELEGATE and OPERATOR users", e.getMessage());
        verifyNoInteractions(coreOnboardingApiRestClient);
    }

    private void verifyRequest(String institutionId, String productId, CreateUserDto createUserDto, ArgumentCaptor<OnboardingInstitutionOperatorsRequest> onboardingRequestCaptor, String userId) {
        OnboardingInstitutionOperatorsRequest request = onboardingRequestCaptor.getValue();
        assertNotNull(request);
        assertEquals(institutionId, request.getInstitutionId());
        assertEquals(productId, request.getProductId());
        assertNotNull(request.getUsers());
        assertEquals(1, request.getUsers().size());
        assertEquals(createUserDto.getName(), request.getUsers().get(0).getName());
        assertEquals(createUserDto.getSurname(), request.getUsers().get(0).getSurname());
        assertEquals(createUserDto.getTaxCode(), request.getUsers().get(0).getTaxCode());
        assertEquals(createUserDto.getEmail(), request.getUsers().get(0).getEmail());
        assertEquals(userId, request.getUsers().get(0).getId());
        createUserDto.getRoles().forEach(role -> request.getUsers().forEach(user -> {
            assertEquals(role.getProductRole(), user.getProductRole());
            assertEquals(role.getPartyRole().name(), user.getRole().name());
        }));
    }


    @Test
    void getGeographicTaxonomyList_nullInstitutionId() {
        // given
        String institutionId = null;
        // when
        Executable executable = () -> msCoreConnector.getGeographicTaxonomyList(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(coreInstitutionApiRestClient);
    }


    @Test
    void getGeographicTaxonomyList_noGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        InstitutionResponse institutionMock = mockInstance(new InstitutionResponse());
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(any()))
                .thenReturn(ResponseEntity.ok(institutionMock));
        // when
        Executable executable = () -> msCoreConnector.getGeographicTaxonomyList(institutionId);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("The institution %s does not have geographic taxonomies.", institutionId), e.getMessage());
        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionByIdUsingGET(institutionId);
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }


    @Test
    void getGeographicTaxonomyList() {
        // given
        String institutionId = "institutionId";
        InstitutionResponse institutionMock = mockInstance(new InstitutionResponse());
        AttributesResponse attribute = mockInstance(new AttributesResponse());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeoTaxonomies())));
        institutionMock.setAttributes(List.of(attribute));
        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(any()))
                .thenReturn(ResponseEntity.ok(institutionMock));
        // when
        List<GeographicTaxonomy> geographicTaxonomies = msCoreConnector.getGeographicTaxonomyList(institutionId);
        // then
        assertSame(institutionMock.getGeographicTaxonomies().size(), geographicTaxonomies.size());
        assertNotNull(geographicTaxonomies);
        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionByIdUsingGET(institutionId);
        verifyNoMoreInteractions(coreInstitutionApiRestClient);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void checkExistingRelationshipRoles_userExistingConflict2 (PartyRole partyRole){
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator Api";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
        UserInfo.UserInfoFilter mockUserInfoFilter = new UserInfo.UserInfoFilter();
        mockUserInfoFilter.setProductId(productId);
        mockUserInfoFilter.setUserId(userId);
        mockUserInfoFilter.setAllowedStates(List.of(ACTIVE));
        List<RelationshipResult> mockRelationshipsResponse = getRelationshipResults();
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(mockRelationshipsResponse));
        // when
        Executable executable = () -> msCoreConnector.checkExistingRelationshipRoles(institutionId, productId, createUserDto, userId);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("User role conflict", e.getMessage());
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(anyString(), any(), any(), any(), any(), any());
    }

    private static List<RelationshipResult> getRelationshipResults() {
        RelationshipResult mockRelationshipInfo = new RelationshipResult();
        mockRelationshipInfo.setFrom("from");
        mockRelationshipInfo.setId("id");
        mockRelationshipInfo.setTo("to");
        it.pagopa.selfcare.core.generated.openapi.v1.dto.ProductInfo productInfo = new it.pagopa.selfcare.core.generated.openapi.v1.dto.ProductInfo();
        productInfo.setId("productId");
        productInfo.setRole("Operator security");
        mockRelationshipInfo.setProduct(productInfo);
        List<RelationshipResult> mockRelationshipsResponse = new ArrayList<>();
        mockRelationshipsResponse.add(mockRelationshipInfo);
        return mockRelationshipsResponse;
    }

    @Test
    void checkExistingRelationshipRoles_userExistingNoConflict () {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(PartyRole.OPERATOR);
        createUserDto.setRoles(Set.of(roleMock));
        UserInfo.UserInfoFilter mockUserInfoFilter = new UserInfo.UserInfoFilter();
        mockUserInfoFilter.setProductId(productId);
        mockUserInfoFilter.setUserId(userId);
        mockUserInfoFilter.setAllowedStates(List.of(ACTIVE));
        RelationshipResult mockRelationshipResult = new RelationshipResult();
        mockRelationshipResult.setFrom("from");
        mockRelationshipResult.setId("id");
        mockRelationshipResult.setTo("to");
        mockRelationshipResult.setRole(RelationshipResult.RoleEnum.valueOf(PartyRole.OPERATOR.name()));
        it.pagopa.selfcare.core.generated.openapi.v1.dto.ProductInfo productInfo = new it.pagopa.selfcare.core.generated.openapi.v1.dto.ProductInfo();
        productInfo.setId("productId");
        productInfo.setRole("Operator Api");
        mockRelationshipResult.setProduct(productInfo);
        List<RelationshipResult> mockRelationshipsResponse = new ArrayList<>();
        mockRelationshipsResponse.add(mockRelationshipResult);
        when(coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(mockRelationshipsResponse));
        // when
        msCoreConnector.checkExistingRelationshipRoles(institutionId, productId, createUserDto, userId);
        // then
        verify(coreInstitutionApiRestClient, times(1))
                ._getUserInstitutionRelationshipsUsingGET(anyString(), any(), any(), any(), any(), any());
    }

    private GetDelegationParameters dummyDelegationParameters() {
        return GetDelegationParameters.builder()
                .to("to")
                .productId("setProductId")
                .taxCode("taxCode")
                .search("name")
                .mode(GetDelegationsMode.FULL.name())
                .order(Order.ASC.name())
                .page(0)
                .size(1000)
                .build();
    }

}