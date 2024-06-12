package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationType;
import it.pagopa.selfcare.dashboard.core.DelegationService;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationIdResource;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationRequestDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.DelegationMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {DelegationController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {DelegationController.class, DelegationMapperImpl.class})
class DelegationControllerTest {

    @MockBean
    private DelegationService delegationService;

    @Autowired
    private MockMvc mvc;


    @Test
    void createDelegationWithRequiredParameter() throws Exception {
        DelegationId delegationId = new DelegationId();
        delegationId.setId("id");

        DelegationIdResource delegationIdResource = new DelegationIdResource();
        delegationIdResource.setId("id");

        when(delegationService.createDelegation(any(DelegationRequest.class))).thenReturn(delegationId);

        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/delegations")
                        .content(objectMapper.writeValueAsString(getDelegationRequestDto()))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(delegationIdResource)))
                .andReturn();
    }

    @Test
    void createDelegationWithFromNull() throws Exception {
        DelegationRequestDto delegationRequestDto = getDelegationRequestDto();
        delegationRequestDto.setFrom(null);

        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/delegations")
                        .content(objectMapper.writeValueAsString(delegationRequestDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void createDelegationWithToNull() throws Exception {
        DelegationRequestDto delegationRequestDto = getDelegationRequestDto();
        delegationRequestDto.setTo(null);

        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/delegations")
                        .content(objectMapper.writeValueAsString(delegationRequestDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void createDelegationWithProductIdNull() throws Exception {
        DelegationRequestDto delegationRequestDto = getDelegationRequestDto();
        delegationRequestDto.setProductId(null);

        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/delegations")
                        .content(objectMapper.writeValueAsString(delegationRequestDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void createDelegationWithInstitutionFromNameNull() throws Exception {
        DelegationRequestDto delegationRequestDto = getDelegationRequestDto();
        delegationRequestDto.setInstitutionFromName(null);

        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/delegations")
                        .content(objectMapper.writeValueAsString(delegationRequestDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void createDelegationWithInstitutionToNameNull() throws Exception {
        DelegationRequestDto delegationRequestDto = getDelegationRequestDto();
        delegationRequestDto.setInstitutionToName(null);

        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/delegations")
                        .content(objectMapper.writeValueAsString(delegationRequestDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void createDelegationWithWrongType() throws Exception {
        DelegationRequestDto delegationRequestDto = getDelegationRequestDto();
        delegationRequestDto.setType(DelegationType.UO);

        ObjectMapper objectMapper = new ObjectMapper();

        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/delegations")
                        .content(objectMapper.writeValueAsString(delegationRequestDto))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    private DelegationRequestDto getDelegationRequestDto() {
        DelegationRequestDto delegationRequestDto = new DelegationRequestDto();
        delegationRequestDto.setFrom("from");
        delegationRequestDto.setTo("to");
        delegationRequestDto.setProductId("productId");
        delegationRequestDto.setInstitutionFromName("institutionFromName");
        delegationRequestDto.setInstitutionToName("institutionToName");
        delegationRequestDto.setType(DelegationType.AOO);
        return delegationRequestDto;
    }
}