package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitution;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.SUSPENDED;
import static it.pagopa.selfcare.dashboard.core.UserGroupServiceImpl.REQUIRED_GROUP_ID_MESSAGE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.support.PageableExecutionUtils.getPage;

@ContextConfiguration(classes = {UserGroupV2ServiceImpl.class})
@ExtendWith(SpringExtension.class)
class UserGroupV2ServiceImplTest {
    @MockBean
    private UserApiConnector userApiConnector;

    @MockBean
    private UserGroupConnector userGroupConnector;

    @Autowired
    private UserGroupV2ServiceImpl userV2GroupServiceImpl;

    @Test
    void createGroup() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        CreateUserGroup userGroup = mockInstance(new CreateUserGroup());
        String id1 = randomUUID().toString();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        List<String> userIds = List.of(id1, id2, id3, id4);

        userGroup.setMembers(userIds);
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");
        String groupIdMock = "groupId";
        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);
        when(userGroupConnector.createUserGroup(any()))
                .thenReturn(groupIdMock);

        when(userApiConnector.getUsers(anyString(), any(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        String groupId = userV2GroupServiceImpl.createUserGroup(userGroup);
        //then
        assertEquals(groupIdMock, groupId);
        verify(userGroupConnector, times(1))
                .createUserGroup(any());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnector, times(1))
                .getUsers(eq(userGroup.getInstitutionId()), filterCaptor.capture(), eq("loggedUserId"));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userGroupConnector, userApiConnector);
    }

    @Test
    void createGroup_invalidList() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        CreateUserGroup userGroup = mockInstance(new CreateUserGroup());
        String id1 = randomUUID().toString();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        List<String> userIds = List.of(randomUUID().toString(), id2, id3, id4);
        userGroup.setMembers(userIds);
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(userApiConnector.getUsers(anyString(), any(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> userV2GroupServiceImpl.createUserGroup(userGroup);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnector, times(1))
                .getUsers(eq(userGroup.getInstitutionId()), filterCaptor.capture(), eq("loggedUserId"));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userApiConnector);
        Mockito.verifyNoInteractions(userGroupConnector);
    }

    @Test
    void delete() {
        // given
        String groupId = "relationshipId";
        // when
        userV2GroupServiceImpl.delete(groupId);
        // then
        verify(userGroupConnector, times(1))
                .delete(groupId);
        verifyNoMoreInteractions(userGroupConnector);
    }

    @Test
    void delete_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> userV2GroupServiceImpl.delete(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnector);
    }

    @Test
    void activate() {
        // given
        String groupId = "relationshipId";
        // when
        userV2GroupServiceImpl.activate(groupId);
        // then
        verify(userGroupConnector, times(1))
                .activate(groupId);
        verifyNoMoreInteractions(userGroupConnector);
    }

    @Test
    void activate_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> userV2GroupServiceImpl.activate(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnector);
    }

    @Test
    void suspend() {
        // given
        String groupId = "relationshipId";
        // when
        userV2GroupServiceImpl.suspend(groupId);
        // then
        verify(userGroupConnector, times(1))
                .suspend(groupId);
        verifyNoMoreInteractions(userGroupConnector);
    }

    @Test
    void suspend_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> userV2GroupServiceImpl.suspend(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnector);
    }

    @Test
    void updateUserGroup() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        String groupId = "groupId";
        UpdateUserGroup userGroup = mockInstance(new UpdateUserGroup());
        String id1 = randomUUID().toString();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        List<String> userIds = List.of(id1, id2, id3, id4);
        userGroup.setMembers(userIds);

        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId");
        foundGroup.setId(groupId);
        when(userGroupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(userApiConnector.getUsers(anyString(), any(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> userV2GroupServiceImpl.updateUserGroup(groupId, userGroup);
        //then
        assertDoesNotThrow(executable);
        verify(userGroupConnector, times(1))
                .getUserGroupById(anyString());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnector, times(1))
                .getUsers(eq(foundGroup.getInstitutionId()), filterCaptor.capture(), eq("loggedUserId"));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(userGroupConnector, times(1))
                .updateUserGroup(anyString(), any());
        verifyNoMoreInteractions(userApiConnector, userGroupConnector);
    }

    @Test
    void updateUserGroup_invalidMembersList() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        String groupId = "groupId";
        UpdateUserGroup userGroup = mockInstance(new UpdateUserGroup());
        String id1 = randomUUID().toString();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        List<String> userIds = List.of(randomUUID().toString(), id2, id3, id4);
        userGroup.setMembers(userIds);

        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId");
        foundGroup.setId(groupId);
        when(userGroupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(userApiConnector.getUsers(anyString(), any(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> userV2GroupServiceImpl.updateUserGroup(groupId, userGroup);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnector, times(1))
                .getUsers(eq(foundGroup.getInstitutionId()), filterCaptor.capture(), eq("loggedUserId"));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(userGroupConnector, times(1))
                .getUserGroupById(anyString());
        verifyNoMoreInteractions(userApiConnector, userGroupConnector);
    }


    @Test
    void testDeleteMembersByUserIdUserNotFound() {
        // Arrange
        when(userApiConnector.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(Collections.emptyList());

        // Act
        userV2GroupServiceImpl.deleteMembersByUserId("userId", "institutionId", "prod-io");

        // Assert that nothing has changed
        verify(userApiConnector).retrieveFilteredUser("userId", "institutionId", "prod-io");
    }


    @Test
    void testDeleteMembersByUserIdUserFound() {
        // Arrange
        when(userApiConnector.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(List.of(Mockito.mock(UserInstitution.class)));

        // Act and Assert
        userV2GroupServiceImpl.deleteMembersByUserId("userId", "institutionId", "prod-io");
        verify(userApiConnector).retrieveFilteredUser("userId", "institutionId", "prod-io");
        verifyNoInteractions(userGroupConnector);
    }

    @Test
    void addMemberToUserGroup() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        String groupId = "groupId";
        String institutionId = "institutionId";
        String productId = "productId";
        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId", "setInstitutionId");
        foundGroup.setId(groupId);
        UUID userId = randomUUID();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(userId.toString());
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        foundGroup.setMembers(List.of(userInfoMock2, userInfoMock3, userInfoMock4));
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId);
        foundGroup.setProductId(productId);

        when(userGroupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        when(userApiConnector.getUsers(anyString(), any(), any()))
                .thenReturn(members);
        //when
        Executable executable = () -> userV2GroupServiceImpl.addMemberToUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        verify(userGroupConnector, times(1))
                .addMemberToUserGroup(anyString(), any());
        verify(userGroupConnector, times(1))
                .getUserGroupById(groupId);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnector, times(1))
                .getUsers(eq(institutionId), filterCaptor.capture(), eq("loggedUserId"));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());

        verifyNoMoreInteractions(userGroupConnector, userApiConnector);
    }

    @Test
    void addMemberToUserGroup_invalidMember() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        String groupId = "groupId";
        String institutionId = "institutionId";
        String productId = "productId";
        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId", "setInstitutionId");
        foundGroup.setId(groupId);
        UUID userId = randomUUID();
        String id1 = randomUUID().toString();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        foundGroup.setMembers(List.of(userInfoMock2, userInfoMock3, userInfoMock4));
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId);
        foundGroup.setProductId(productId);

        when(userGroupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        when(userApiConnector.getUsers(anyString(), any(), any()))
                .thenReturn(members);
        //when
        Executable executable = () -> userV2GroupServiceImpl.addMemberToUserGroup(groupId, userId);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("This user is not allowed for this group", e.getMessage());
        verify(userGroupConnector, times(1))
                .getUserGroupById(groupId);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnector, times(1))
                .getUsers(eq(institutionId), filterCaptor.capture(), eq("loggedUserId"));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userGroupConnector, userApiConnector);
    }

    @Test
    void addMemberToUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = randomUUID();
        //when
        Executable executable = () -> userV2GroupServiceImpl.addMemberToUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnector);
    }

    @Test
    void addMemberToUserGroup_nullUserId() {
        //given
        String groupId = "groupId";
        UUID userId = null;
        //when
        Executable executable = () -> userV2GroupServiceImpl.addMemberToUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnector);
    }

    @Test
    void deleteMemberFromUserGroup() {
        //given
        String groupId = "groupId";
        UUID userId = randomUUID();
        //when
        Executable executable = () -> userV2GroupServiceImpl.deleteMemberFromUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        verify(userGroupConnector, times(1))
                .deleteMemberFromUserGroup(anyString(), any());
        verifyNoMoreInteractions(userGroupConnector);
    }

    @Test
    void deleteMemberFromUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = randomUUID();
        //when
        Executable executable = () -> userV2GroupServiceImpl.deleteMemberFromUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnector);
    }

    @Test
    void deleteMemberFromUserGroup_nullUserId() {
        //given
        String groupId = "groupId";
        UUID userId = null;
        //when
        Executable executable = () -> userV2GroupServiceImpl.deleteMemberFromUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnector);
    }

    @Test
    void getUserGroups() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UUID userId = randomUUID();
        Pageable pageable = PageRequest.of(1, 2);
        UserGroupInfo userGroupInfo = mockInstance(new UserGroupInfo());
        when(userGroupConnector.getUserGroups(any(), any()))
                .thenAnswer(invocation -> getPage(List.of(userGroupInfo), invocation.getArgument(1, Pageable.class), () -> 1L));
        //when
        Page<UserGroupInfo> groupInfos = userV2GroupServiceImpl.getUserGroups(institutionId, productId, userId, pageable);
        //then
        assertNotNull(groupInfos);
        assertNotNull(groupInfos.getContent());
        assertEquals(1, groupInfos.getContent().size());
        verify(userGroupConnector, times(1))
                .getUserGroups(any(), any());
        verifyNoMoreInteractions(userGroupConnector);
    }

    @Test
    void getUserGroupById() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        String groupId = "groupId";
        String institutionId = "institutionId";
        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId", "setInstitutionId", "setCreatedBy", "setModifiedBy");
        foundGroup.setId(groupId);
        String id1 = randomUUID().toString();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        when(userApiConnector.getUsers(anyString(), any(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId);
        User createdBy = new User();
        createdBy.setId("createdBy");
        foundGroup.setCreatedBy(createdBy);
        User modifiedBy = new User();
        modifiedBy.setId("modifiedBy");
        foundGroup.setModifiedBy(modifiedBy);

        User createdByMock = mockInstance(new User(), "setId");
        User modifiedByMock = mockInstance(new User(), "setId");

        when(userGroupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);
        when(userApiConnector.getUserById(anyString(), anyString(), any()))
                .thenAnswer(invocation -> {
                    User userMock = new User();
                    userMock.setId(invocation.getArgument(0, String.class));
                    return userMock;
                });
        when(userApiConnector.getUserById(eq(createdBy.getId()), anyString(), any()))
                .thenAnswer(invocation -> {
                    createdByMock.setId(invocation.getArgument(0, String.class));
                    return createdByMock;
                });
        when(userApiConnector.getUserById(eq(modifiedBy.getId()), anyString(), any()))
                .thenAnswer(invocation -> {
                    modifiedByMock.setId(invocation.getArgument(0, String.class));
                    return modifiedByMock;
                });
        //when
        UserGroupInfo groupInfo = userV2GroupServiceImpl.getUserGroupById(groupId, institutionId);
        //then
        assertEquals(foundGroup.getId(), groupInfo.getId());
        assertEquals(foundGroup.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(foundGroup.getProductId(), groupInfo.getProductId());
        assertEquals(foundGroup.getStatus(), groupInfo.getStatus());
        assertEquals(foundGroup.getDescription(), groupInfo.getDescription());
        assertEquals(foundGroup.getName(), groupInfo.getName());
        assertEquals(foundGroup.getMembers(), groupInfo.getMembers());
        assertEquals(foundGroup.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(createdByMock.getId(), groupInfo.getCreatedBy().getId());
        assertEquals(foundGroup.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(modifiedByMock.getId(), groupInfo.getModifiedBy().getId());
        verify(userGroupConnector, times(1))
                .getUserGroupById(anyString());
        verify(userApiConnector, times(6))
                .getUserById(any(), any(),anyList());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnector, times(1))
                .getUsers(eq(institutionId), filterCaptor.capture(), eq("loggedUserId"));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userGroupConnector);
    }

    @Test
    void getUserGroupById_nullModifiedBy() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        String groupId = "groupId";
        String institutionId = "institutionId";
        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId", "setInstitutionId", "setCreatedBy", "setModifiedBy");
        foundGroup.setId(groupId);
        String id1 = randomUUID().toString();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        when(userApiConnector.getUsers(anyString(), any(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId);
        User createdBy = new User();
        createdBy.setId("createdBy");
        foundGroup.setCreatedBy(createdBy);

        User createdByMock = mockInstance(new User(), "setId");

        when(userGroupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);
        when(userApiConnector.getUserById(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    User userMock = new User();
                    userMock.setId(invocation.getArgument(0, String.class));
                    return userMock;
                });
        when(userApiConnector.getUserById(any(), any(), any()))
                .thenAnswer(invocation -> {
                    createdByMock.setId(invocation.getArgument(0, String.class));
                    return createdByMock;
                });
        //when
        UserGroupInfo groupInfo = userV2GroupServiceImpl.getUserGroupById(groupId, institutionId);
        //then
        assertEquals(foundGroup.getId(), groupInfo.getId());
        assertEquals(foundGroup.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(foundGroup.getProductId(), groupInfo.getProductId());
        assertEquals(foundGroup.getStatus(), groupInfo.getStatus());
        assertEquals(foundGroup.getDescription(), groupInfo.getDescription());
        assertEquals(foundGroup.getName(), groupInfo.getName());
        assertEquals(foundGroup.getMembers(), groupInfo.getMembers());
        assertEquals(foundGroup.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(createdByMock.getId(), groupInfo.getCreatedBy().getId());
        assertEquals(foundGroup.getModifiedAt(), groupInfo.getModifiedAt());
        assertNull(groupInfo.getModifiedBy());
        verify(userGroupConnector, times(1))
                .getUserGroupById(anyString());
        verifyNoMoreInteractions(userGroupConnector);
    }

    @Test
    void getUserGroupById_noRelationshipMember() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("loggedUserId").build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given
        String groupId = "groupId";
        String institutionId = "institutionId";
        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId", "setInstitutionId", "setCreatedBy", "setModifiedBy");
        foundGroup.setId(groupId);
        String id1 = randomUUID().toString();
        String id2 = randomUUID().toString();
        String id3 = randomUUID().toString();
        String id4 = randomUUID().toString();
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        when(userApiConnector.getUsers(any(), any(), any()))
                .thenReturn(List.of(userInfoMock4));
        when(userApiConnector.getUserById(anyString(), any(), any()))
                .thenAnswer(invocation -> {
                    User userMock = new User();
                    userMock.setId(invocation.getArgument(0, String.class));
                    return userMock;
                });

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId);
        final String createdById = randomUUID().toString();
        final String modifiedById = randomUUID().toString();
        User createdBy = new User();
        createdBy.setId(createdById);
        foundGroup.setCreatedBy(createdBy);
        User modifiedBy = new User();
        modifiedBy.setId(modifiedById);
        foundGroup.setModifiedBy(modifiedBy);

        User createdByMock = mockInstance(new User(), "setId");
        createdByMock.setId(createdById);
        User modifiedByMock = mockInstance(new User(), "setId");
        modifiedByMock.setId(modifiedById);

        when(userGroupConnector.getUserGroupById(any()))
                .thenReturn(foundGroup);
        when(userApiConnector.getUserById(eq(createdById), any(), any()))
                .thenReturn(createdByMock);
        when(userApiConnector.getUserById(eq(modifiedById), any(), any()))
                .thenReturn(modifiedByMock);
        //when
        UserGroupInfo groupInfo = userV2GroupServiceImpl.getUserGroupById(groupId, institutionId);
        //then
        assertEquals(foundGroup.getId(), groupInfo.getId());
        assertEquals(foundGroup.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(foundGroup.getProductId(), groupInfo.getProductId());
        assertEquals(foundGroup.getStatus(), groupInfo.getStatus());
        assertEquals(foundGroup.getDescription(), groupInfo.getDescription());
        assertEquals(foundGroup.getName(), groupInfo.getName());
        assertEquals(1, groupInfo.getMembers().size());
        assertEquals(foundGroup.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(createdByMock.getId(), groupInfo.getCreatedBy().getId());
        assertEquals(foundGroup.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(modifiedByMock.getId(), groupInfo.getModifiedBy().getId());
        verify(userGroupConnector, times(1))
                .getUserGroupById(groupId);

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnector, times(1))
                .getUsers(eq(institutionId), filterCaptor.capture(), eq("loggedUserId"));
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userGroupConnector);
    }

    @Test
    void getUserGroupById_nullId() {
        //given
        String groupId = null;
        String institutionId = "institutionId";
        //when
        Executable executable = () -> userV2GroupServiceImpl.getUserGroupById(groupId, institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnector);
        Mockito.verifyNoInteractions(userApiConnector);

    }

    @Test
    void getUserGroupById_invalidUserGroupException() {
        //given
        String groupId = "groupId";
        String institutionId = "institutionId";
        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId");
        foundGroup.setId(groupId);
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        userInfoMock1.setId("createdBy");
        userInfoMock2.setId("modifiedBy");
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());

        when(userGroupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);
        //when
        Executable executable = () -> userV2GroupServiceImpl.getUserGroupById(groupId, institutionId);
        //then
        InvalidUserGroupException e = assertThrows(InvalidUserGroupException.class, executable);
        assertEquals("Could not find a UserGroup for given institutionId", e.getMessage());
        verify(userGroupConnector, times(1))
                .getUserGroupById(anyString());
        verifyNoMoreInteractions(userGroupConnector);
        Mockito.verifyNoInteractions(userApiConnector);
    }

    @Test
    void testDeleteMembersByUserIdUserNotFound1() {
        // Arrange
        when(userApiConnector.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(Collections.emptyList());

        // Act
        userV2GroupServiceImpl.deleteMembersByUserId("userId", "institutionId", "prod-io");

        // Assert that nothing has changed
        verify(userApiConnector).retrieveFilteredUser("userId", "institutionId", "prod-io");
    }


    @Test
    void testDeleteMembersByUserIdUserFound2() {
        // Arrange
        when(userApiConnector.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(List.of(Mockito.mock(UserInstitution.class)));

        // Act and Assert
        userV2GroupServiceImpl.deleteMembersByUserId("userId", "institutionId", "prod-io");
        verify(userApiConnector).retrieveFilteredUser("userId", "institutionId", "prod-io");
        verifyNoInteractions(userGroupConnector);
    }
}
