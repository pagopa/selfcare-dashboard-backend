package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.InstitutionV2Service;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {InstitutionV2Controller.class})
@WebMvcTest(value = {InstitutionV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class InstitutionV2ControllerTest {

    private static final String BASE_URL = "/v2/institutions";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private InstitutionV2Controller institutionV2Controller;

    @MockBean
    private InstitutionV2Service institutionV2ServiceMock;

    @Test
    void getInstitutionUser_notNullUser() throws Exception {
        //given
        String institutionId = "institutionId";
        String userId = "notFound";
        UserInfo userInfo = mockInstance(new UserInfo(), "setId");
        userInfo.setId(randomUUID().toString());

        String loggedUserId = "loggedUserId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(loggedUserId).build());

        when(institutionV2ServiceMock.getInstitutionUser(any(), any(), any()))
                .thenReturn(userInfo);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/users/{userId}", institutionId, userId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        //then
        InstitutionUserResource userResource = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionUserResource.class);
        assertNotNull(userResource);
        verify(institutionV2ServiceMock, times(1))
                .getInstitutionUser(institutionId, userId, loggedUserId);
        verifyNoMoreInteractions(institutionV2ServiceMock);

    }
}
