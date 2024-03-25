package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.UserGroupV2Service;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.handler.DashboardExceptionsHandler;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UpdateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupIdResource;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupResource;
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

import java.time.Instant;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.data.support.PageableExecutionUtils.getPage;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {UserGroupV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserGroupV2Controller.class, WebTestConfig.class, DashboardExceptionsHandler.class})
class UserGroupV2ControllerTest {
    private static final String BASE_URL = "/v2/user-groups";

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
        when(groupServiceMock.createUserGroup(any())).
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
        String userId = "userId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(userId).build());
        String groupId = "groupId";
        UpdateUserGroupDto groupDto = TestUtils.mockInstance(new UpdateUserGroupDto());
        Set<UUID> mockMembers = Set.of(UUID.randomUUID());
        groupDto.setMembers(mockMembers);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .put(BASE_URL + "/" + groupId)
                .principal(authentication)
                .content(mapper.writeValueAsString(groupDto))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        verify(groupServiceMock, times(1))
                .updateUserGroup(Mockito.anyString(), Mockito.notNull());
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void getUserGroup() throws Exception {
        //given
        String userId = "userId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(userId).build());
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
        when(groupServiceMock.getUserGroupById(Mockito.anyString(), any()))
                .thenReturn(model);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/" + groupId)
                .principal(authentication)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        UserGroupResource response = mapper.readValue(result.getResponse().getContentAsString(), UserGroupResource.class);
        assertNotNull(response);
        verify(groupServiceMock, times(1))
                .getUserGroupById(Mockito.anyString(), any());
        verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void addMemberToUserGroup() throws Exception {
        //given
        String userId = "loggedUserId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(userId).build());
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/" + groupId + "/members/" + memberId)
                .principal(authentication)
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
        //given
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
        when(groupServiceMock.getUserGroups(any(), any(), any(), any()))
                .thenAnswer(invocation -> getPage(List.of(model), invocation.getArgument(3, Pageable.class), () -> 1L));
        //when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
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
                .getUserGroups(any(), any(), any(), isNotNull());
        verifyNoMoreInteractions(groupServiceMock);
    }
}