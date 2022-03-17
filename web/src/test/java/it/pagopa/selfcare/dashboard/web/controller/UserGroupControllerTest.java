package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.core.UserGroupService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.handler.DashboardExceptionsHandler;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UpdateUserGroupDto;
import org.junit.jupiter.api.Test;
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

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(value = {UserGroupController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserGroupController.class, WebTestConfig.class, DashboardExceptionsHandler.class})
class UserGroupControllerTest {
    private static final String BASE_URL = "/user-groups";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @MockBean
    private UserGroupService groupServiceMock;

    @Test
    void createGroup() throws Exception {
        //given
        CreateUserGroupDto dto = TestUtils.mockInstance(new CreateUserGroupDto());
        Set<UUID> mockMembers = Set.of(UUID.randomUUID());
        dto.setMembers(mockMembers);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/")
                .content(mapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .createUserGroup(Mockito.notNull());
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void deleteGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/" + groupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .delete(groupId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void activateUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/" + groupId + "/activate")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .activate(groupId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void suspendUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/" + groupId + "/suspend")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .suspend(groupId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void updateUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        UpdateUserGroupDto groupDto = TestUtils.mockInstance(new UpdateUserGroupDto());
        Set<UUID> mockMembers = Set.of(UUID.randomUUID());
        groupDto.setMembers(mockMembers);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .put(BASE_URL + "/" + groupId)
                .content(mapper.writeValueAsString(groupDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .updateUserGroup(Mockito.anyString(), Mockito.notNull());
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }
}