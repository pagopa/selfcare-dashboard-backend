package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.core.DelegationService;
import it.pagopa.selfcare.dashboard.core.FileStorageService;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.handler.DashboardExceptionsHandler;
import it.pagopa.selfcare.dashboard.web.model.GeographicTaxonomyListDto;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.DelegationMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.product.ProductsResource;
import it.pagopa.selfcare.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.MimeTypeUtils;

import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {InstitutionController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {InstitutionController.class, DelegationMapperImpl.class, WebTestConfig.class, DashboardExceptionsHandler.class, InstitutionResourceMapperImpl.class})
class InstitutionControllerTest {

    private static final String BASE_URL = "/v1/institutions";
    private static final ProductTree PRODUCT_TREE;
    private static final Product PRODUCT;

    static {
        PRODUCT_TREE = new ProductTree();
        PRODUCT = new Product();
        PRODUCT.setId("id1");
        PRODUCT_TREE.setChildren(List.of(PRODUCT));
        PRODUCT_TREE.setNode(PRODUCT);
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @InjectMocks
    private InstitutionController institutionController;

    @MockBean
    private FileStorageService storageServiceMock;

    @MockBean
    private InstitutionService institutionServiceMock;

    @MockBean
    private DelegationService delegationService;

    @Test
    void saveInstitutionLogo() throws Exception {
        // given
        String institutionId = "institutionId";
        String contentType = MimeTypeUtils.IMAGE_JPEG_VALUE;
        String filename = "test.jpeg";
        MockMultipartFile multipartFile = new MockMultipartFile("logo", filename,
                contentType, "test institution logo".getBytes());
        MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart(BASE_URL + "/" + institutionId + "/logo")
                .file(multipartFile);
        requestBuilder.with(request -> {
            request.setMethod(HttpMethod.PUT.name());
            return request;
        });
        // when
        mvc.perform(requestBuilder)
                .andExpect(status().isOk());
        // then
        verify(storageServiceMock, times(1))
                .storeInstitutionLogo(Mockito.eq(institutionId), any(), Mockito.eq(contentType), Mockito.eq(filename));
        verifyNoMoreInteractions(storageServiceMock);
    }

    @Test
    void getInstitution_institutionInfoNotNull() throws Exception {
        // given
        String institutionId = "institutionId";
        UpdateInstitutionResource resource = mockInstance(new UpdateInstitutionResource());
        when(institutionServiceMock.findInstitutionById(anyString()))
                .thenAnswer(invocationOnMock -> {
                    String id = invocationOnMock.getArgument(0, String.class);
                    Institution institution = mockInstance(new Institution(), "setExternalId");
                    institution.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
                    institution.setExternalId(id);
                    return institution;
                });
        // when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}", institutionId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        assertNotNull(resource);
        verify(institutionServiceMock, times(1))
                .findInstitutionById(institutionId);
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getProductsTree_notNull() throws Exception {
        // given
        String institutionId = "institutionId";
        when(institutionServiceMock.getProductsTree())
                .thenReturn(List.of(PRODUCT_TREE));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/products", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<ProductsResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertEquals(1, products.get(0).getChildren().size());
        verify(institutionServiceMock, times(1))
                .getProductsTree();
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getProductsTree_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        when(institutionServiceMock.getProductsTree())
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/products", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<ProductsResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(institutionServiceMock, times(1))
                .getProductsTree();
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void updateInstitutionGeographicTaxonomy(@Value("classpath:stubs/geographicTaxonomyListDto.json") Resource geographicTaxonomyDto) throws Exception {
        // given
        String institutionId = "institutionId";
        Mockito.doNothing()
                .when(institutionServiceMock).updateInstitutionGeographicTaxonomy(anyString(), any());
        GeographicTaxonomyListDto geographicTaxonomyListDto = objectMapper.readValue(
                geographicTaxonomyDto.getInputStream().readAllBytes(),
                GeographicTaxonomyListDto.class
        );
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/" + institutionId + "/geographicTaxonomy")
                        .content(geographicTaxonomyDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        assertEquals("", result.getResponse().getContentAsString());
        ArgumentCaptor<GeographicTaxonomyList> argumentCaptor = ArgumentCaptor.forClass(GeographicTaxonomyList.class);
        verify(institutionServiceMock, times(1))
                .updateInstitutionGeographicTaxonomy(Mockito.eq(institutionId), argumentCaptor.capture());
        GeographicTaxonomyList geographicTaxonomyList = argumentCaptor.getValue();
        assertEquals(geographicTaxonomyListDto.getGeographicTaxonomyDtoList().get(0).getCode(), geographicTaxonomyList.getGeographicTaxonomyList().get(0).getCode());
        assertEquals(geographicTaxonomyListDto.getGeographicTaxonomyDtoList().get(0).getDesc(), geographicTaxonomyList.getGeographicTaxonomyList().get(0).getDesc());
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionGeographicTaxonomy() throws Exception {
        // given
        String institutionId = "institutionId";
        List<GeographicTaxonomy> geographicTaxonomyListMock = List.of(mockInstance(new GeographicTaxonomy()));
        when(institutionServiceMock.getGeographicTaxonomyList(Mockito.anyString()))
                .thenReturn(geographicTaxonomyListMock);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/geographicTaxonomy", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        List<GeographicTaxonomy> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertEquals(geographicTaxonomyListMock.get(0).getCode(), response.get(0).getCode());
        assertEquals(geographicTaxonomyListMock.get(0).getDesc(), response.get(0).getDesc());
        verify(institutionServiceMock, times(1))
                .getGeographicTaxonomyList(institutionId);
        verifyNoMoreInteractions(institutionServiceMock);
    }
    
    @Test
    void updateInstitutionDescription_ok() throws Exception {
        //given
        String institutionId = "setId";
        UpdateInstitutionResource resource = mockInstance(new UpdateInstitutionResource());
        Institution institutionMock = mockInstance(new Institution());
        when(institutionServiceMock.updateInstitutionDescription(anyString(), any())).thenReturn(institutionMock);

        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/" + institutionId)
                        .content(objectMapper.writeValueAsString(resource))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        //then
        Institution institutionResult = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertNotNull(institutionResult);
        assertEquals(institutionResult.getId(), institutionId);
        assertEquals(institutionResult.getDescription(), resource.getDescription());
        assertEquals(institutionResult.getDigitalAddress(), resource.getDigitalAddress());
        verify(institutionServiceMock, times(1))
                .updateInstitutionDescription(institutionId, resource);
        verifyNoMoreInteractions(institutionServiceMock);
    }

    /**
     * Method under test: {@link InstitutionController#getDelegationsUsingFrom(String, String)}
     */
    @Test
    void getDelegationsUsingFrom_shouldGetData() throws Exception {
        // Given
        Delegation expectedDelegation = dummyDelegation();

        when(delegationService.getDelegations(any())).thenReturn(List.of(expectedDelegation));
        // When

        MvcResult result = mvc
                .perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/partners?productId={productId}", expectedDelegation.getInstitutionId(), expectedDelegation.getProductId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andReturn();

        List<DelegationResource> resource = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        // Then
        assertThat(resource).isNotNull();
        org.assertj.core.api.Assertions.assertThat(resource).hasSize(1);
        DelegationResource actual = resource.get(0);
        assertThat(actual.getId()).isEqualTo(expectedDelegation.getId());
        assertThat(actual.getInstitutionName()).isEqualTo(expectedDelegation.getInstitutionName());
        assertThat(actual.getBrokerName()).isEqualTo(expectedDelegation.getBrokerName());
        assertThat(actual.getBrokerId()).isEqualTo(expectedDelegation.getBrokerId());
        assertThat(actual.getProductId()).isEqualTo(expectedDelegation.getProductId());
        assertThat(actual.getInstitutionId()).isEqualTo(expectedDelegation.getInstitutionId());

        verify(delegationService, times(1))
                .getDelegations(dummyDelegationParametersFrom());
        verifyNoMoreInteractions(delegationService);
    }

    /**
     * Method under test: {@link InstitutionController#getDelegationsUsingTo(String, String, String, String, GetDelegationsMode, Order, Integer, Integer)}
     */
    @Test
    void getDelegationsUsingTo_shouldGetData() throws Exception {
        // Given
        Delegation expectedDelegation = dummyDelegation();
        GetDelegationParameters delegationParameters = dummyDelegationParametersTo();

        when(delegationService.getDelegations(any())).thenReturn(List.of(expectedDelegation));
        // When

        MvcResult result = mvc
                .perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/institutions?productId={productId}&search={search}&taxCode={taxCode}&mode=FULL&order=ASC&page={page}&size={size}",
                                delegationParameters.getTo(), delegationParameters.getProductId(), delegationParameters.getSearch(),
                                delegationParameters.getTaxCode(), delegationParameters.getPage(), delegationParameters.getSize()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andReturn();

        List<DelegationResource> resource = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        // Then
        assertThat(resource).isNotNull();
        org.assertj.core.api.Assertions.assertThat(resource).hasSize(1);
        DelegationResource actual = resource.get(0);
        assertThat(actual.getId()).isEqualTo(expectedDelegation.getId());
        assertThat(actual.getInstitutionName()).isEqualTo(expectedDelegation.getInstitutionName());
        assertThat(actual.getBrokerName()).isEqualTo(expectedDelegation.getBrokerName());
        assertThat(actual.getBrokerId()).isEqualTo(expectedDelegation.getBrokerId());
        assertThat(actual.getProductId()).isEqualTo(expectedDelegation.getProductId());
        assertThat(actual.getInstitutionId()).isEqualTo(expectedDelegation.getInstitutionId());

        verify(delegationService, times(1))
                .getDelegations(dummyDelegationParametersTo());
        verifyNoMoreInteractions(delegationService);
    }

    @Test
    void getDelegationsUsingTo_shouldGetDataWithoutFilters() throws Exception {
        // Given
        Delegation expectedDelegation = dummyDelegation();

        GetDelegationParameters delegationParameters = GetDelegationParameters.builder()
                .to("to")
                .build();

        when(delegationService.getDelegations(any())).thenReturn(List.of(expectedDelegation));
        // When

        MvcResult result = mvc
                .perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/institutions", expectedDelegation.getBrokerId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andReturn();

        List<DelegationResource> resource = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        // Then
        assertThat(resource).isNotNull();
        org.assertj.core.api.Assertions.assertThat(resource).hasSize(1);
        DelegationResource actual = resource.get(0);
        assertThat(actual.getId()).isEqualTo(expectedDelegation.getId());
        assertThat(actual.getInstitutionName()).isEqualTo(expectedDelegation.getInstitutionName());
        assertThat(actual.getInstitutionRootName()).isEqualTo(expectedDelegation.getInstitutionRootName());
        assertThat(actual.getBrokerName()).isEqualTo(expectedDelegation.getBrokerName());
        assertThat(actual.getBrokerId()).isEqualTo(expectedDelegation.getBrokerId());
        assertThat(actual.getProductId()).isEqualTo(expectedDelegation.getProductId());
        assertThat(actual.getInstitutionId()).isEqualTo(expectedDelegation.getInstitutionId());

        verify(delegationService, times(1))
                .getDelegations(delegationParameters);
        verifyNoMoreInteractions(delegationService);
    }

    private Delegation dummyDelegation() {
        Delegation delegation = new Delegation();
        delegation.setInstitutionId("from");
        delegation.setBrokerId("to");
        delegation.setId("setId");
        delegation.setProductId("setProductId");
        delegation.setType(DelegationType.PT);
        delegation.setInstitutionName("setInstitutionFromName");
        return delegation;
    }

    private GetDelegationParameters dummyDelegationParametersTo() {
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

    private GetDelegationParameters dummyDelegationParametersFrom() {
        return GetDelegationParameters.builder()
                .from("from")
                .productId("setProductId")
                .build();
    }

}