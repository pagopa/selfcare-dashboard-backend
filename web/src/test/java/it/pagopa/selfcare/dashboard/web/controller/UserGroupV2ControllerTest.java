package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.core.UserGroupV2Service;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.handler.DashboardExceptionsHandler;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UpdateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupIdResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.data.support.PageableExecutionUtils.getPage;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {UserGroupV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserGroupV2Controller.class, WebTestConfig.class, DashboardExceptionsHandler.class})
class UserGroupV2ControllerTest {
    private static final String BASE_URL = "/v2/user-groups";

    private static final String FILE_JSON_PATH = "src/test/resources/json/";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @MockBean
    private UserGroupV2Service groupServiceMock;

    @Test
    void createGroup() throws Exception {
        //given
        String userId = "userId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(userId).build());
        CreateUserGroupDto dto = TestUtils.mockInstance(new CreateUserGroupDto());
        Set<UUID> mockMembers = Set.of(UUID.randomUUID());
        dto.setMembers(mockMembers);
        String groupId = "groupId";
        when(groupServiceMock.createUserGroup(any(CreateUserGroup.class))).
                thenReturn(groupId);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/")
                        .principal(authentication)
                .content(mapper.writeValueAsString(dto))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();
        //then
        UserGroupIdResource resource = mapper.readValue(result.getResponse().getContentAsString(), UserGroupIdResource.class);
        assertNotNull(resource);
        assertEquals(groupId, resource.getId());
        assertEquals(0, result.getResponse().getContentLength());
        verify(groupServiceMock, times(1))
                .createUserGroup(Mockito.notNull());
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void deleteGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/" + groupId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        verify(groupServiceMock, times(1))
                .delete(groupId);
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void activateUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/" + groupId + "/activate")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        verify(groupServiceMock, times(1))
                .activate(groupId);
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void suspendUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/" + groupId + "/suspend")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        verify(groupServiceMock, times(1))
                .suspend(groupId);
        verifyNoMoreInteractions(groupServiceMock);
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
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        verify(groupServiceMock, times(1))
                .updateUserGroup(eq(groupId), Mockito.notNull());
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void getUserGroup() throws Exception {
        //given
        String groupId = "groupId";

        byte[] userGroupInfoStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserGroupInfo.json"));
        UserGroupInfo userGroupInfo = mapper.readValue(userGroupInfoStream, UserGroupInfo.class);

        when(groupServiceMock.getUserGroupById(groupId, null))
                .thenReturn(userGroupInfo);
        //when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/" + groupId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(new String(Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserGroupResource.json")))));

        //then
        verify(groupServiceMock, times(1))
                .getUserGroupById(groupId, null);
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void getUserGroupWithInstitutionId() throws Exception {
        //given
        String groupId = "groupId";
        String inst = "institutionId";

        byte[] userGroupInfoStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserGroupInfo.json"));
        UserGroupInfo userGroupInfo = mapper.readValue(userGroupInfoStream, UserGroupInfo.class);

        when(groupServiceMock.getUserGroupById(groupId, inst))
                .thenReturn(userGroupInfo);
        //when
        mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/" + groupId)
                        .queryParam("institutionId", inst)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(new String(Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserGroupResource.json")))));

        //then
        verify(groupServiceMock, times(1))
                .getUserGroupById(groupId, inst);
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void addMemberToUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/" + groupId + "/members/" + memberId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        verify(groupServiceMock, times(1))
                .addMemberToUserGroup(groupId, memberId);
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void deleteMemberFromUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/" + groupId + "/members/" + memberId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        verify(groupServiceMock, times(1))
                .deleteMemberFromUserGroup(groupId, memberId);
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void getUserGroups() throws Exception {
        String instId = "institutionId";
        String productId = "prod-io";
        UUID userId = UUID.randomUUID();

        //given
        byte[] userGroupInfoStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserGroupInfo.json"));
        UserGroupInfo userGroupInfo = mapper.readValue(userGroupInfoStream, UserGroupInfo.class);

        when(groupServiceMock.getUserGroups(eq(instId), eq(productId), eq(userId), any()))
                .thenAnswer(invocation -> getPage(List.of(userGroupInfo), invocation.getArgument(3, Pageable.class), () -> 1L));
        //when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
                        .queryParam("institutionId", instId)
                        .queryParam("productId", productId)
                        .queryParam("userId", userId.toString())
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number", notNullValue()))
                .andExpect(jsonPath("$.size", notNullValue()))
                .andExpect(jsonPath("$.totalElements", notNullValue()))
                .andExpect(jsonPath("$.totalPages", notNullValue()))
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].id", notNullValue()))
                .andExpect(jsonPath("$.content[0].institutionId", notNullValue()))
                .andExpect(jsonPath("$.content[0].productId", notNullValue()))
                .andExpect(jsonPath("$.content[0].name", notNullValue()))
                .andExpect(jsonPath("$.content[0].description", notNullValue()))
                .andExpect(jsonPath("$.content[0].status", notNullValue()))
                .andExpect(jsonPath("$.content[0].membersCount", notNullValue()))
                .andExpect(jsonPath("$.content[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.content[0].createdBy", notNullValue()))
                .andExpect(jsonPath("$.content[0].modifiedAt", notNullValue()))
                .andExpect(jsonPath("$.content[0].modifiedBy", notNullValue()));
        //then
        verify(groupServiceMock, times(1))
                .getUserGroups(eq(instId), eq(productId), eq(userId), any());
        verifyNoMoreInteractions(groupServiceMock);
    }
}