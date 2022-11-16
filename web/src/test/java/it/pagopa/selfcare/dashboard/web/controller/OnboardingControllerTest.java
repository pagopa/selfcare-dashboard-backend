package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContact;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.handler.DashboardExceptionsHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {OnboardingController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {OnboardingController.class, WebTestConfig.class, DashboardExceptionsHandler.class})
class OnboardingControllerTest {

    private static final String BASE_URL = "/onboarding-requests";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private InstitutionService institutionServiceMock;


    @Test
    void retrieveOnboardingRequest() throws Exception {
        // given
        String tokenId = UUID.randomUUID().toString();
        final OnboardingRequestInfo dto = mockInstance(new OnboardingRequestInfo());
        dto.getManager().getUser().setId(UUID.randomUUID().toString());
        dto.getManager().getUser().setWorkContacts(Map.of(dto.getInstitutionInfo().getId(), mockInstance(new WorkContact())));
        dto.setAdmins(List.of(dto.getManager()));
        when(institutionServiceMock.getOnboardingRequestInfo(any()))
                .thenReturn(dto);
        // when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{tokenId}", tokenId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.id", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.name", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.institutionType", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.address", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.zipCode", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.mailAddress", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.fiscalCode", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.vatNumber", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.recipientCode", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.pspData", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.pspData.businessRegisterNumber", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.pspData.legalRegisterName", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.pspData.legalRegisterNumber", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.pspData.abiCode", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.pspData.vatNumberGroup", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.dpoData", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.dpoData.address", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.dpoData.pec", notNullValue()))
                .andExpect(jsonPath("$.institutionInfo.dpoData.email", notNullValue()))
                .andExpect(jsonPath("$.manager", notNullValue()))
                .andExpect(jsonPath("$.manager.id", notNullValue()))
                .andExpect(jsonPath("$.manager.name", notNullValue()))
                .andExpect(jsonPath("$.manager.surname", notNullValue()))
                .andExpect(jsonPath("$.manager.email", notNullValue()))
                .andExpect(jsonPath("$.manager.fiscalCode", notNullValue()))
                .andExpect(jsonPath("$.admins", notNullValue()))
                .andExpect(jsonPath("$.admins[0].id", notNullValue()))
                .andExpect(jsonPath("$.admins[0].name", notNullValue()))
                .andExpect(jsonPath("$.admins[0].surname", notNullValue()))
                .andExpect(jsonPath("$.admins[0].email", notNullValue()))
                .andExpect(jsonPath("$.admins[0].fiscalCode", notNullValue()));
        // then
        verify(institutionServiceMock, times(1))
                .getOnboardingRequestInfo(tokenId);
        verifyNoMoreInteractions(institutionServiceMock);
    }

}