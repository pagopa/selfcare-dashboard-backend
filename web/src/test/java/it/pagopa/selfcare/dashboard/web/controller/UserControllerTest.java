package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.UserService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.SearchUserDto;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(value = {UserController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserController.class, WebTestConfig.class})
class UserControllerTest {

    private static final String BASE_URL = "/v1/users";
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

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @MockBean
    private UserService userServiceMock;


    @Test
    void search_notNull() throws Exception {
        //given
        String externalId = "externalId";
        String institutionId = "institutionId";
        SearchUserDto externalIdDto = new SearchUserDto();
        externalIdDto.setFiscalCode(externalId);
        Mockito.when(userServiceMock.search(Mockito.anyString()))
                .thenReturn(USER_RESOURCE);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/search")
                .queryParam("institutionId", institutionId)
                .content(mapper.writeValueAsString(externalIdDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        UserResource userResponse = mapper.readValue(result.getResponse().getContentAsString(), UserResource.class);
        assertNotNull(userResponse);
        Mockito.verify(userServiceMock, Mockito.times(1))
                .search(externalId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void updateUser(@Value("classpath:stubs/updateUserDto.json") Resource updateUserDto) throws Exception {
        //given
        UUID id = UUID.randomUUID();
        String institutionId = "institutionId";
        //when
        mvc.perform(MockMvcRequestBuilders
                .put(BASE_URL + "/{id}", id)
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
    void getUserByInternalId() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        String institutionId = "institutionId";
        Mockito.when(userServiceMock.getUserByInternalId(Mockito.any()))
                .thenReturn(USER_RESOURCE);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{id}", id)
                .queryParam("institutionId", institutionId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        UserResource userResponse = mapper.readValue(result.getResponse().getContentAsString(), UserResource.class);
        assertNotNull(userResponse);
        assertEquals(USER_RESOURCE.getWorkContacts().get(institutionId).getEmail().getValue(), userResponse.getEmail().getValue());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .getUserByInternalId(id);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void saveUser(@Value("classpath:stubs/userDto.json") Resource userDto) throws Exception {
        //given
        String institutionId = "institutionId";
        UserId id = TestUtils.mockInstance(new UserId());
        Mockito.when(userServiceMock.saveUser(Mockito.anyString(), Mockito.any()))
                .thenReturn(id);
        //when
        mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/")
                .content(userDto.getInputStream().readAllBytes())
                .queryParam("institutionId", institutionId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id.getId().toString())));
        //then
        verify(userServiceMock, times(1))
                .saveUser(eq(institutionId), any(SaveUserDto.class));
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }


    @Test
    void deleteUserById() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .deleteById(id.toString());
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

}