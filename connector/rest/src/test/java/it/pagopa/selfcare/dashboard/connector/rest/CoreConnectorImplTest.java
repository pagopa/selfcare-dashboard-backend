package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.PageInfo;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.*;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreDelegationApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreManagementApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreOnboardingApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.DelegationRestClientMapperImpl;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.validation.ValidationException;
import java.util.*;
import java.util.function.Function;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.commons.utils.TestUtils.checkNotNullFields;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.rest.CoreConnectorImpl.REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE;
import static it.pagopa.selfcare.dashboard.connector.rest.CoreConnectorImpl.REQUIRED_INSTITUTION_ID_MESSAGE;
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
    private CoreDelegationApiRestClient coreDelegationApiRestClient;

    @MockBean
    private CoreInstitutionApiRestClient coreInstitutionApiRestClient;

    @MockBean
    private CoreOnboardingApiRestClient coreOnboardingApiRestClient;

    @MockBean
    private CoreManagementApiRestClient coreManagementApiRestClient;
    @MockBean
    private BrokerMapper brokerMapper;

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

    @Test
    void getDelegationsV2_shouldGetData() {
        // given
        DelegationResponse delegationResponse = dummyDelegationResponse();
        List<DelegationResponse> delegationResponseList = new ArrayList<>();
        delegationResponseList.add(delegationResponse);
        PageInfo pageInfo = new PageInfo(1L, 0L, 1L, 1L);
        DelegationWithPaginationResponse delegationWithPaginationResponse = new DelegationWithPaginationResponse(delegationResponseList, pageInfo);
        ResponseEntity<DelegationWithPaginationResponse> delegationResponseEntity = new ResponseEntity<>(delegationWithPaginationResponse, null, HttpStatus.OK);
        GetDelegationParameters parameters = dummyDelegationParameters();

        when(coreDelegationApiRestClient._getDelegationsUsingGET1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(delegationResponseEntity);


        // when
        DelegationWithPagination response = msCoreConnector.getDelegationsV2(dummyDelegationParameters());
        // then
        assertNotNull(response);
        assertEquals(1, response.getDelegations().size());

        assertEquals(delegationResponseList.get(0).getId(), response.getDelegations().get(0).getId());
        assertEquals(delegationResponseList.get(0).getInstitutionId(), response.getDelegations().get(0).getInstitutionId());
        assertEquals(delegationResponseList.get(0).getBrokerId(), response.getDelegations().get(0).getBrokerId());
        assertEquals(delegationResponseList.get(0).getProductId(), response.getDelegations().get(0).getProductId());
        assertEquals(delegationResponseList.get(0).getType().toString(), response.getDelegations().get(0).getType().toString());
        assertEquals(delegationResponseList.get(0).getInstitutionName(), response.getDelegations().get(0).getInstitutionName());
        assertEquals(delegationResponseList.get(0).getBrokerName(), response.getDelegations().get(0).getBrokerName());

        verify(coreDelegationApiRestClient, times(1))
                ._getDelegationsUsingGET1(parameters.getFrom(), parameters.getTo(), parameters.getProductId(), parameters.getSearch(), parameters.getTaxCode(), parameters.getMode(), parameters.getOrder(), parameters.getPage(), parameters.getSize());
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


    @ParameterizedTest
    @EnumSource(value = it.pagopa.selfcare.commons.base.security.PartyRole.class)
    void party2SelcRoleMapping(it.pagopa.selfcare.commons.base.security.PartyRole partyRole) {
        // when
        SelfCareAuthority authority = partyRole.getSelfCareAuthority();
        // then
        assertEquals(PARTY_2_SELC_ROLE.apply(partyRole), authority);
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