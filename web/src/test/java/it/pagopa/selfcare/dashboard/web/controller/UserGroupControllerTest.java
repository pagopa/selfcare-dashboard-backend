package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.UserGroupService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.handler.DashboardExceptionsHandler;
import it.pagopa.selfcare.dashboard.web.model.user_groups.*;
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

import java.time.Instant;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

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
        String groupId = "groupId";
        Mockito.when(groupServiceMock.createUserGroup(Mockito.any())).
                thenReturn(groupId);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        //then
        UserGroupIdResource resource = mapper.readValue(result.getResponse().getContentAsString(), UserGroupIdResource.class);
        assertNotNull(resource);
        assertEquals(groupId, resource.getId());
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

    @Test
    void getUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        UserGroupInfo model = TestUtils.mockInstance(new UserGroupInfo());
        UserInfo userInfoModel = TestUtils.mockInstance(new UserInfo());
        userInfoModel.setId(UUID.randomUUID().toString());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        userInfoModel.setProducts(productInfoMap);
        model.setMembers(List.of(userInfoModel));
        User createdBy = mockInstance(new User(), "setId");
        createdBy.setId(randomUUID().toString());
        model.setCreatedBy(createdBy);
        User modifiendBy = mockInstance(new User(), "setId");
        modifiendBy.setId(randomUUID().toString());
        model.setModifiedBy(modifiendBy);
        Instant now = Instant.now();
        model.setModifiedAt(now);
        model.setCreatedAt(now);
        Mockito.when(groupServiceMock.getUserGroupById(Mockito.anyString(), Mockito.any()))
                .thenReturn(model);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/" + groupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //then
        UserGroupResource response = mapper.readValue(result.getResponse().getContentAsString(), UserGroupResource.class);
        assertNotNull(response);
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .getUserGroupById(Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void addMemberToUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/" + groupId + "/members/" + memberId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .addMemberToUserGroup(groupId, memberId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void deleteMemberFromUserGroup() throws Exception {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/" + groupId + "/members/" + memberId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .deleteMemberFromUserGroup(groupId, memberId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void getUserGroups() throws Exception {
        //given
        String groupId = "groupId";
        UserGroupInfo model = TestUtils.mockInstance(new UserGroupInfo());
        UserInfo userInfoModel = TestUtils.mockInstance(new UserInfo());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        userInfoModel.setProducts(productInfoMap);
        model.setMembers(List.of(userInfoModel));
        User userModel = TestUtils.mockInstance(new User());
        userModel.setId(UUID.randomUUID().toString());
        model.setCreatedBy(userModel);
        model.setModifiedBy(userModel);
        Instant now = Instant.now();
        model.setModifiedAt(now);
        model.setCreatedAt(now);
        Mockito.when(groupServiceMock.getUserGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(model));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //then
        List<UserGroupPlainResource> groups = mapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(groups);
        assertFalse(groups.isEmpty());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .getUserGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.isNotNull());
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }
}