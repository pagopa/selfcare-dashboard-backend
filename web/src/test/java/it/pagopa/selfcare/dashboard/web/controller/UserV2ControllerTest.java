package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.Certification;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContact;
import it.pagopa.selfcare.dashboard.core.UserService;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.InstitutionBaseResource;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.GET_INSTITUTION_MODE;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapperImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {UserV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserV2Controller.class, InstitutionResourceMapperImpl.class, WebTestConfig.class})
class UserV2ControllerTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private UserV2Service userServiceMock;


    @Autowired
    protected ObjectMapper objectMapper;

    private static final String BASE_URL = "/v2";

    private static final User USER_RESOURCE;

    static {
        USER_RESOURCE = TestUtils.mockInstance(new User());
        USER_RESOURCE.setId(UUID.randomUUID().toString());
        Map<String, WorkContact> workContacts = new HashMap<>();
        WorkContact workContact = TestUtils.mockInstance(new WorkContact());
        workContact.getEmail().setCertification(Certification.SPID);
        workContacts.put("institutionId", workContact);
        USER_RESOURCE.setWorkContacts(workContacts);
    }


    @Test
    void updateUser(@Value("classpath:stubs/updateUserDto.json") Resource updateUserDto) throws Exception {
        //given
        UUID id = UUID.randomUUID();
        String institutionId = "institutionId";
        //when
        mvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/users/{id}", id)
                        .queryParam("institutionId", institutionId)
                        .content(updateUserDto.getInputStream().readAllBytes())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        //then
        verify(userServiceMock, times(1))
                .updateUser(eq(id), eq(institutionId), any(MutableUserFieldsDto.class));
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void getInstitutions_institutionInfoNotNull() throws Exception {
        // given
        String userId = "userId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(userId).build());

        InstitutionInfo expectedInstitution = mockInstance(new InstitutionInfo());
        expectedInstitution.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        List<InstitutionInfo> expectedInstitutionInfos = new ArrayList<>();
        expectedInstitutionInfos.add(expectedInstitution);
        when(userServiceMock.getInstitutions(userId)).thenReturn(expectedInstitutionInfos);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/institutions")
                        .principal(authentication)
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
        assertEquals(resources.get(0).getStatus(), expectedInstitution.getStatus().name());
        assertNotNull(resources.get(0).getUserRole());
        verify(userServiceMock, times(1))
                .getInstitutions(userId);
        verifyNoMoreInteractions(userServiceMock);
    }


}
