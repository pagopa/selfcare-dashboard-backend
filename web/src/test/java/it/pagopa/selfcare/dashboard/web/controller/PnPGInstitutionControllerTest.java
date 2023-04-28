package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.core.PnPGInstitutionService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.handler.DashboardExceptionsHandler;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductsResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {PnPGInstitutionController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {PnPGInstitutionController.class, WebTestConfig.class, DashboardExceptionsHandler.class})
class PnPGInstitutionControllerTest {

    private static final String BASE_URL = "/pnPGInstitutions";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private PnPGInstitutionService pnPGInstitutionServiceMock;

    @Test
    void getPnPGInstitutions_institutionInfoNotNull() throws Exception {
        // given
        when(pnPGInstitutionServiceMock.getInstitutions())
                .thenAnswer(invocationOnMock -> {
                    List<InstitutionInfo> listOfInstitutionInfo = List.of(mockInstance(new InstitutionInfo()));
                    listOfInstitutionInfo.get(0).setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
                    System.out.println(listOfInstitutionInfo);
                    return listOfInstitutionInfo;
                });
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/")
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(resources);
        assertFalse(resources.isEmpty());
        verify(pnPGInstitutionServiceMock, times(1))
                .getInstitutions();
        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
    }

    @Test
    void getPnPGInstitutionProducts_notNull() throws Exception {
        // given
        String institutionId = "institutionId";
        List<PartyProduct> products = List.of(mockInstance(new PartyProduct()));
        when(pnPGInstitutionServiceMock.getInstitutionProducts(any()))
                .thenReturn(products);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/products", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<PartyProduct> results = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(results.isEmpty());
        verify(pnPGInstitutionServiceMock, times(1))
                .getInstitutionProducts(institutionId);
        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
    }

    @Test
    void getPnPGInstitutionProducts_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        when(pnPGInstitutionServiceMock.getInstitutionProducts(any()))
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/products", institutionId)
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
        verify(pnPGInstitutionServiceMock, times(1))
                .getInstitutionProducts(institutionId);
        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
    }

    @Test
    void updateInstitutionDescription_ok() throws Exception {
        //given
        String institutionId = "institutionId";
        String description = "description";
        Mockito.doNothing()
                .when(pnPGInstitutionServiceMock).updateInstitutionDescription(anyString(), anyString());

        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/" + institutionId + "/description?description=" + description)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        //then
        assertEquals("", result.getResponse().getContentAsString());
        verify(pnPGInstitutionServiceMock, times(1))
                .updateInstitutionDescription(institutionId, description);
        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
    }

}