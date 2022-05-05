package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.UserService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.EmbeddedExternalIdDto;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.user.UserIdResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@WebMvcTest(value = {UserController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserController.class, WebTestConfig.class})
class UserControllerTest {

    private static final String BASE_URL = "/users";
    private static final User USER_RESOURCE;

    static {
        USER_RESOURCE = TestUtils.mockInstance(new User());
        USER_RESOURCE.setId(UUID.randomUUID().toString());
        Map<String, WorkContactResource> workContacts = new HashMap<>();
        WorkContactResource workContact = TestUtils.mockInstance(new WorkContactResource());
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

    @Captor
    private ArgumentCaptor<UserDto> userDtoCaptor;

    @Captor
    private ArgumentCaptor<UUID> uidCaptor;


    @Test
    void search_notNull() throws Exception {
        //given
        String externalId = "externalId";
        String institutionId = "institutionId";
        EmbeddedExternalIdDto externalIdDto = new EmbeddedExternalIdDto();
        externalIdDto.setExternalId(externalId);
        Mockito.when(userServiceMock.search(Mockito.anyString()))
                .thenReturn(USER_RESOURCE);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/search")
                        .queryParam("institutionId", institutionId)
                        .content(mapper.writeValueAsString(externalIdDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //then
        UserResource userResponse = mapper.readValue(result.getResponse().getContentAsString(), UserResource.class);
        assertNotNull(userResponse);
        Mockito.verify(userServiceMock, Mockito.times(1))
                .search(externalId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void updateUser() throws Exception {
        //given
        UUID id = UUID.randomUUID();
        String institutionId = "institutionId";
        UpdateUserDto updateUserDto = TestUtils.mockInstance(new UpdateUserDto());
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/{id}", id)
                        .queryParam("institutionId", institutionId)
                        .content(mapper.writeValueAsString(updateUserDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .updateUser(uidCaptor.capture(), Mockito.anyString(), userDtoCaptor.capture());
        UserDto dto = userDtoCaptor.getValue();
        assertEquals(dto.getEmail(), updateUserDto.getEmail());
        assertTrue(dto.getWorkContacts().containsKey(institutionId));
        assertEquals(dto.getFamilyName(), updateUserDto.getSurname());
        assertEquals(dto.getName(), updateUserDto.getName());
        assertEquals(dto.getWorkContacts().get(institutionId).getEmail(), updateUserDto.getEmail());

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
                .andExpect(MockMvcResultMatchers.status().isOk())
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
    void saveUser() throws Exception {
        //given
        String institutionId = "institutionId";
        it.pagopa.selfcare.dashboard.web.model.user.UserDto dto = TestUtils.mockInstance(new it.pagopa.selfcare.dashboard.web.model.user.UserDto());
        UserId id = TestUtils.mockInstance(new UserId());
        Mockito.when(userServiceMock.saveUser(Mockito.anyString(), Mockito.any()))
                .thenReturn(id);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/save-user")
                        .content(mapper.writeValueAsString(dto))
                        .queryParam("institutionId", institutionId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        //then
        UserIdResource idResource = mapper.readValue(result.getResponse().getContentAsString(), UserIdResource.class);
        assertEquals(id.getId(), idResource.getId());
        ArgumentCaptor<SaveUser> captureSave = ArgumentCaptor.forClass(SaveUser.class);
        Mockito.verify(userServiceMock, Mockito.times(1))
                .saveUser(Mockito.eq(institutionId), captureSave.capture());
        SaveUser saveUser = captureSave.getValue();
        assertEquals(dto.getEmail(), saveUser.getEmail());
        assertEquals(dto.getName(), saveUser.getName());
        assertEquals(dto.getSurname(), saveUser.getFamilyName());
        assertEquals(dto.getFiscalCode(), saveUser.getFiscalCode());
        assertTrue(saveUser.getWorkContacts().containsKey(institutionId));
        assertEquals(dto.getEmail(), saveUser.getWorkContacts().get(institutionId).getEmail());
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
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .deleteById(id.toString());
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

}