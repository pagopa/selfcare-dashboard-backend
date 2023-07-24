package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationType;
import it.pagopa.selfcare.dashboard.core.DelegationService;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationIdResource;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationRequestDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.DelegationMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.DelegationMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {DelegationController.class})
@ExtendWith(MockitoExtension.class)
class DelegationControllerTest {

    @InjectMocks
    private DelegationController delegationController;

    @Mock
    private DelegationService delegationService;

    @Spy
    private DelegationMapper delegationMapper = new DelegationMapperImpl();

    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Method under test: {@link DelegationController#createDelegation(DelegationRequestDto)}
     */
    @Test
    void testCreateDelegation() throws Exception {

        DelegationId delegation = new DelegationId();
        delegation.setId("id");
        when(delegationService.createDelegation(any())).thenReturn(delegation);

        DelegationRequestDto delegationRequest = new DelegationRequestDto();
        delegationRequest.setFrom("111111");
        delegationRequest.setTo("2222222");
        delegationRequest.setInstitutionFromName("Test name");
        delegationRequest.setProductId("productId");
        delegationRequest.setType(DelegationType.PT);
        String content = (new ObjectMapper()).writeValueAsString(delegationRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/delegations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        MvcResult result =  MockMvcBuilders.standaloneSetup(delegationController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andReturn();

        DelegationIdResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(delegation.getId(), response.getId());
    }

    /**
     * Method under test: {@link DelegationController#createDelegation(DelegationRequestDto)}
     */
    @Test
    void testCreateDelegationWithBadRequest() throws Exception {

        DelegationRequestDto delegationRequest = new DelegationRequestDto();
        String content = (new ObjectMapper()).writeValueAsString(delegationRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/delegations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        MockMvcBuilders.standaloneSetup(delegationController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * Method under test: {@link DelegationController#createDelegation(DelegationRequestDto)}
     */
    @Test
    void testCreateDelegationBadType() throws Exception {

        DelegationRequestDto delegationRequest = new DelegationRequestDto();
        delegationRequest.setFrom("111111");
        delegationRequest.setTo("2222222");
        delegationRequest.setInstitutionFromName("Test name");
        delegationRequest.setProductId("productId");
        delegationRequest.setType(DelegationType.UO);
        String content = (new ObjectMapper()).writeValueAsString(delegationRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/delegations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        MockMvcBuilders.standaloneSetup(delegationController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}