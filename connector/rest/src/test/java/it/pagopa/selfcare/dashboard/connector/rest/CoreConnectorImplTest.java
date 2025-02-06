package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.*;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationWithPagination;
import it.pagopa.selfcare.dashboard.connector.model.delegation.GetDelegationParameters;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.*;
import static it.pagopa.selfcare.dashboard.connector.rest.CoreConnectorImpl.REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE;
import static it.pagopa.selfcare.dashboard.connector.rest.CoreConnectorImpl.REQUIRED_INSTITUTION_ID_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CoreConnectorImplTest extends BaseConnectorTest{

    @Spy
    private final InstitutionMapperImpl institutionMapperSpy = new InstitutionMapperImpl();

    @Spy
    private final DelegationRestClientMapperImpl delegationRestClientMapperSpy = new DelegationRestClientMapperImpl();

    @InjectMocks
    private CoreConnectorImpl msCoreConnector;

    @Mock
    private CoreDelegationApiRestClient coreDelegationApiRestClient;

    @Mock
    private CoreInstitutionApiRestClient coreInstitutionApiRestClient;

    @Mock
    private CoreOnboardingApiRestClient coreOnboardingApiRestClient;

    @Mock
    private CoreManagementApiRestClient coreManagementApiRestClient;
    @Mock
    private BrokerMapper brokerMapper;

    @BeforeEach
    public void setup() {
        super.setUp();
    }

    private static final Function<it.pagopa.selfcare.commons.base.security.PartyRole, SelfCareAuthority> PARTY_2_SELC_ROLE = partyRole -> switch (partyRole) {
        case MANAGER, DELEGATE, SUB_DELEGATE -> ADMIN;
        case ADMIN_EA -> ADMIN_EA;
        default -> LIMITED;
    };


    @Test
    void getInstitution_nullInstitutionId() {
        // when
        Executable executable = () -> msCoreConnector.getInstitution(null);
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
    void getInstitution() throws IOException {
        // given
        String institutionId = "institutionId";
        ClassPathResource resource = new ClassPathResource("stubs/InstitutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse institutionMock = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId))
                .thenReturn(ResponseEntity.of(Optional.of(institutionMock)));
        // when
        Institution institution = msCoreConnector.getInstitution(institutionId);
        // then

        assertEquals(institution, institutionMapperSpy.toInstitution(institutionMock));
        verify(coreInstitutionApiRestClient, times(1))
                ._retrieveInstitutionByIdUsingGET(institutionId);
    }

    @Test
    void getInstitutionsFromTaxCode() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("stubs/InstitutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse institutionMock = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        InstitutionsResponse institutionsResponse = new InstitutionsResponse();
        institutionsResponse.setInstitutions(List.of(institutionMock));

        when(coreInstitutionApiRestClient._getInstitutionsUsingGET(institutionMock.getTaxCode(), null, null, null, null))
                .thenReturn(ResponseEntity.of(Optional.of(institutionsResponse)));
        // when
        List<Institution> institutions = msCoreConnector.getInstitutionsFromTaxCode(institutionMock.getTaxCode(), null, null, null);
        // then

        assertEquals(institutions.get(0), institutionMapperSpy.toInstitution(institutionMock));
        verify(coreInstitutionApiRestClient, times(1))
                ._getInstitutionsUsingGET(institutionMock.getTaxCode(), null, null, null, null);
    }

    @Test
    void updateInstitutionDescription() throws IOException {
        // given
        String institutionId = "institutionId";
        UpdateInstitutionResource updateResource = new UpdateInstitutionResource();
        updateResource.setDescription("description");
        updateResource.setDigitalAddress("digitalAddress");

        InstitutionPut resourcePut = new InstitutionPut();
        resourcePut.setDescription("description");
        resourcePut.setDigitalAddress("digitalAddress");
        resourcePut.setGeographicTaxonomyCodes(null);

        ClassPathResource resource = new ClassPathResource("stubs/InstitutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        InstitutionResponse institutionMock = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(coreInstitutionApiRestClient._updateInstitutionUsingPUT(institutionId, resourcePut))
                .thenReturn(ResponseEntity.of(Optional.of(institutionMock)));
        // when
        Institution institution = msCoreConnector.updateInstitutionDescription(institutionId, updateResource);
        // then

        assertEquals(institution, institutionMapperSpy.toInstitution(institutionMock));
        verify(coreInstitutionApiRestClient, times(1))
                ._updateInstitutionUsingPUT(institutionId, resourcePut);
    }

    @Test
    void createDelegation() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("stubs/DelegationRequest.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        DelegationRequest delegationMock = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        ClassPathResource responseResource = new ClassPathResource("stubs/DelegationResponse.json");
        byte[] responseResourceStream = Files.readAllBytes(responseResource.getFile().toPath());
        DelegationResponse delegationResponse = objectMapper.readValue(responseResourceStream, new TypeReference<>() {});

        when(coreDelegationApiRestClient._createDelegationUsingPOST(delegationMock))
                .thenReturn(ResponseEntity.of(Optional.of(delegationResponse)));

        ClassPathResource requestResource = new ClassPathResource("stubs/ConnectorDelegationRequest.json");
        byte[] requestResourceStream = Files.readAllBytes(requestResource.getFile().toPath());
        it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest delegationRequest = objectMapper.readValue(requestResourceStream, new TypeReference<>() {});

        DelegationId response = msCoreConnector.createDelegation(delegationRequest);
        assertNotNull(response);
        assertEquals(response.getId(), delegationRequest.getId());
    }

    @Test
    void createDelegation_idNull() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("stubs/DelegationRequest.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        DelegationRequest delegationMock = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        ClassPathResource responseResource = new ClassPathResource("stubs/DelegationResponse_idNull.json");
        byte[] responseResourceStream = Files.readAllBytes(responseResource.getFile().toPath());
        DelegationResponse delegationResponse = objectMapper.readValue(responseResourceStream, new TypeReference<>() {});

        when(coreDelegationApiRestClient._createDelegationUsingPOST(delegationMock))
                .thenReturn(ResponseEntity.of(Optional.of(delegationResponse)));

        ClassPathResource requestResource = new ClassPathResource("stubs/ConnectorDelegationRequest_idNull.json");
        byte[] requestResourceStream = Files.readAllBytes(requestResource.getFile().toPath());
        it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest delegationRequest = objectMapper.readValue(requestResourceStream, new TypeReference<>() {});

        DelegationId response = msCoreConnector.createDelegation(delegationRequest);
        assertNotNull(response);
        assertNull(response.getId());
    }

    @Test
    void createDelegation_empty() throws IOException {
        // given
        ClassPathResource resource = new ClassPathResource("stubs/DelegationRequest.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        DelegationRequest delegationMock = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        when(coreDelegationApiRestClient._createDelegationUsingPOST(delegationMock))
                .thenReturn(ResponseEntity.of(Optional.empty()));

        ClassPathResource requestResource = new ClassPathResource("stubs/ConnectorDelegationRequest.json");
        byte[] requestResourceStream = Files.readAllBytes(requestResource.getFile().toPath());
        it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest delegationRequest = objectMapper.readValue(requestResourceStream, new TypeReference<>() {});

        DelegationId response = msCoreConnector.createDelegation(delegationRequest);
        assertEquals(new DelegationId(), response);
    }

    @Test
    void findInstitutionsByProductIdAndType() throws IOException {
        // given
        final String productId = "prod";
        final String type = "PT";
        ClassPathResource resource = new ClassPathResource("stubs/BrokerInfo.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        BrokerInfo brokerInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        ClassPathResource responseResource = new ClassPathResource("stubs/BrokerResponse.json");
        byte[] responseResourceStream = Files.readAllBytes(responseResource.getFile().toPath());
        BrokerResponse brokerResponse = objectMapper.readValue(responseResourceStream, new TypeReference<>() {});

        when(brokerMapper.fromInstitutions(List.of(brokerResponse))).thenReturn(List.of(brokerInfo));
        when(coreInstitutionApiRestClient._getInstitutionBrokersUsingGET(productId, type))
                .thenReturn(ResponseEntity.of(Optional.of(List.of(brokerResponse))));
        // when
        List<BrokerInfo> response = msCoreConnector.findInstitutionsByProductAndType(productId, type);
        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertNotNull(response.get(0));
        assertEquals(response.get(0), brokerInfo);

    }

    @Test
    void getDelegationUsingFrom_shouldGetData() throws IOException {
        GetDelegationParameters parameters = GetDelegationParameters.builder()
                .productId("setProductId")
                .taxCode("taxCode")
                .search("name")
                .order("ASC")
                .page(0)
                .size(1000)
                .build();

        // Read the expected response from a JSON file
        ClassPathResource resource = new ClassPathResource("stubs/delegationUsingFromExpectedResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        DelegationResponse expectedResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        List < DelegationResponse > delegationResponseList = new ArrayList<>();
        delegationResponseList.add(expectedResponse);

        ResponseEntity<List<DelegationResponse>> delegationResponseEntity = new ResponseEntity<>(delegationResponseList, null, HttpStatus.OK);

        when(coreDelegationApiRestClient._getDelegationsUsingGET(null, null, "setProductId", "name", "taxCode", "ASC", 0, 1000))
                .thenReturn(delegationResponseEntity);

        // when
        List<Delegation> delegationList = msCoreConnector.getDelegations(parameters);

        // then
        assertNotNull(delegationList);
        assertEquals(1, delegationList.size());

        assertEquals(delegationRestClientMapperSpy.toDelegations(expectedResponse), delegationList.get(0));

        verify(coreDelegationApiRestClient, times(1))
                ._getDelegationsUsingGET(parameters.getFrom(), parameters.getTo(), parameters.getProductId(), parameters.getSearch(), parameters.getTaxCode(), parameters.getOrder(), parameters.getPage(), parameters.getSize());
        verifyNoMoreInteractions(coreDelegationApiRestClient);
    }
    @Test
    void getDelegationUsingFrom_shouldGetEmptyData() {
        // given
        GetDelegationParameters parameters = GetDelegationParameters.builder()
                .productId("setProductId")
                .taxCode("taxCode")
                .search("name")
                .order("ASC")
                .page(0)
                .size(1000)
                .build();

        when(coreDelegationApiRestClient._getDelegationsUsingGET(null, null, "setProductId", "name", "taxCode", "ASC", 0, 1000))
                .thenReturn(ResponseEntity.of(Optional.empty()));

        // when
        List<Delegation> delegationList = msCoreConnector.getDelegations(parameters);
        // then
        assertNotNull(delegationList);
        assertEquals(0, delegationList.size());

        verify(coreDelegationApiRestClient, times(1))
                ._getDelegationsUsingGET(parameters.getFrom(), parameters.getTo(), parameters.getProductId(), parameters.getSearch(), parameters.getTaxCode(), parameters.getOrder(), parameters.getPage(), parameters.getSize());
        verifyNoMoreInteractions(coreDelegationApiRestClient);
    }

    @Test
    void getDelegationsV2_shouldGetData() throws IOException {
        // given
        GetDelegationParameters parameters = GetDelegationParameters.builder()
                .productId("setProductId")
                .taxCode("taxCode")
                .search("name")
                .order("ASC")
                .page(0)
                .size(1000)
                .build();
        // Read the expected response from a JSON file
        ClassPathResource resource = new ClassPathResource("stubs/delegationUsingFromExpectedResponseV2.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        DelegationResponse expectedResponse = new ObjectMapper().readValue(resourceStream, new TypeReference<>() {});
        List<DelegationResponse> delegationResponseList = new ArrayList<>();
        delegationResponseList.add(expectedResponse);
        PageInfo pageInfo = new PageInfo(1L, 0L, 1L, 1L);
        DelegationWithPaginationResponse delegationWithPaginationResponse = new DelegationWithPaginationResponse(delegationResponseList, pageInfo);
        ResponseEntity<DelegationWithPaginationResponse> delegationResponseEntity = new ResponseEntity<>(delegationWithPaginationResponse, null, HttpStatus.OK);

        when(coreDelegationApiRestClient._getDelegationsUsingGET1(null, null, "setProductId", "name", "taxCode", "ASC", 0, 1000))
                .thenReturn(delegationResponseEntity);

        // when
        DelegationWithPagination response = msCoreConnector.getDelegationsV2(parameters);

        // then
        assertNotNull(response);
        assertEquals(1, response.getDelegations().size());
        assertEquals(delegationRestClientMapperSpy.toDelegationsWithInfo(expectedResponse), response.getDelegations().get(0));
        verify(coreDelegationApiRestClient, times(1))
                ._getDelegationsUsingGET1(parameters.getFrom(), parameters.getTo(), parameters.getProductId(), parameters.getSearch(), parameters.getTaxCode(), parameters.getOrder(), parameters.getPage(), parameters.getSize());
        verifyNoMoreInteractions(coreDelegationApiRestClient);
    }

    @Test
    void updateGeographicTaxonomy() {
        // given
        String institutionId = "institutionId";
        GeographicTaxonomyList geographicTaxonomiesMock = new GeographicTaxonomyList();
        GeographicTaxonomy geographicTaxonomy = new GeographicTaxonomy();
        geographicTaxonomy.setCode("code");
        geographicTaxonomy.setDesc("desc");
        geographicTaxonomiesMock.setGeographicTaxonomyList(List.of(geographicTaxonomy));
        InstitutionPut resourcePut = new InstitutionPut();
        resourcePut.setGeographicTaxonomyCodes(new ArrayList<>(List.of(geographicTaxonomy.getCode())));
        when(coreInstitutionApiRestClient._updateInstitutionUsingPUT(institutionId, resourcePut)).thenReturn(ResponseEntity.ok().build());
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
        GeographicTaxonomyList geographicTaxonomiesMock = new GeographicTaxonomyList();
        // when
        Executable executable = () -> msCoreConnector.updateInstitutionGeographicTaxonomy(null, geographicTaxonomiesMock);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(coreInstitutionApiRestClient);
    }

    @Test
    void updateGeographicTaxonomy_hasNullGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        // when
        Executable executable = () -> msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, null);
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
        String states = "ACTIVE,PENDING";
        OnboardedProducts products = new OnboardedProducts();
        products.setProducts(List.of(new InstitutionProduct()));
        when(coreInstitutionApiRestClient._retrieveInstitutionProductsUsingGET(institutionId, states))
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
        //when
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setUserId(null);
        filter.setProductRoles(null);
        filter.setProductId(null);
        filter.setRole(null);
        filter.setAllowedStates(null);
        //then
        assertNull(filter.getProductId());
        assertNull(filter.getProductRoles());
        assertNull(filter.getUserId());
        assertNull(filter.getRole());
        assertNull(filter.getAllowedStates());
    }
}