package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.config.CoreTestConfig;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.SUSPENDED;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;
import static it.pagopa.selfcare.dashboard.core.UserGroupServiceImpl.REQUIRED_GROUP_ID_MESSAGE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.support.PageableExecutionUtils.getPage;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserGroupServiceImpl.class, CoreTestConfig.class})
class UserGroupServiceImplTest {

    @Autowired
    private UserGroupService groupService;

    @MockBean
    private UserGroupConnector groupConnector;

    @MockBean
    private MsCoreConnector partyConnector;

    @MockBean
    private UserRegistryConnector userRegistryConnector;

    @Captor
    private ArgumentCaptor<Throwable> throwableCaptor;

    @SpyBean
    private SimpleAsyncUncaughtExceptionHandler simpleAsyncUncaughtExceptionHandler;

    @Test
    void createGroup() {
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
        when(groupConnector.createUserGroup(any()))
                .thenReturn(groupIdMock);

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        String groupId = groupService.createUserGroup(userGroup);
        //then
        assertEquals(groupIdMock, groupId);
        verify(groupConnector, times(1))
                .createUserGroup(any());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(userGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(groupConnector, partyConnector);
    }

    @Test
    void createGroup_invalidList() {
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

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.createUserGroup(userGroup);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(userGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(partyConnector);
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void delete() {
        // given
        String groupId = "relationshipId";
        // when
        groupService.delete(groupId);
        // then
        verify(groupConnector, times(1))
                .delete(groupId);
        verifyNoMoreInteractions(groupConnector);
    }

    @Test
    void delete_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> groupService.delete(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void activate() {
        // given
        String groupId = "relationshipId";
        // when
        groupService.activate(groupId);
        // then
        verify(groupConnector, times(1))
                .activate(groupId);
        verifyNoMoreInteractions(groupConnector);
    }

    @Test
    void activate_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> groupService.activate(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void suspend() {
        // given
        String groupId = "relationshipId";
        // when
        groupService.suspend(groupId);
        // then
        verify(groupConnector, times(1))
                .suspend(groupId);
        verifyNoMoreInteractions(groupConnector);
    }

    @Test
    void suspend_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> groupService.suspend(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void updateUserGroup() {
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
        when(groupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.updateUserGroup(groupId, userGroup);
        //then
        assertDoesNotThrow(executable);
        verify(groupConnector, times(1))
                .getUserGroupById(anyString());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(foundGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(groupConnector, times(1))
                .updateUserGroup(anyString(), any());
        verifyNoMoreInteractions(partyConnector, groupConnector);
    }

    @Test
    void updateUserGroup_invalidMembersList() {
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
        when(groupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.updateUserGroup(groupId, userGroup);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(foundGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(groupConnector, times(1))
                .getUserGroupById(anyString());
        verifyNoMoreInteractions(partyConnector, groupConnector);
    }

    @Test
    void getUserGroupById() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
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

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId.get());
        User createdBy = new User();
        createdBy.setId("createdBy");
        foundGroup.setCreatedBy(createdBy);
        User modifiedBy = new User();
        modifiedBy.setId("modifiedBy");
        foundGroup.setModifiedBy(modifiedBy);

        User createdByMock = mockInstance(new User(), "setId");
        User modifiedByMock = mockInstance(new User(), "setId");

        when(groupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);
        when(userRegistryConnector.getUserByInternalId(anyString(), any()))
                .thenAnswer(invocation -> {
                    User userMock = new User();
                    userMock.setId(invocation.getArgument(0, String.class));
                    return userMock;
                });
        when(userRegistryConnector.getUserByInternalId(eq(createdBy.getId()), any()))
                .thenAnswer(invocation -> {
                    createdByMock.setId(invocation.getArgument(0, String.class));
                    return createdByMock;
                });
        when(userRegistryConnector.getUserByInternalId(eq(modifiedBy.getId()), any()))
                .thenAnswer(invocation -> {
                    modifiedByMock.setId(invocation.getArgument(0, String.class));
                    return modifiedByMock;
                });
        //when
        UserGroupInfo groupInfo = groupService.getUserGroupById(groupId, institutionId);
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
        verify(groupConnector, times(1))
                .getUserGroupById(anyString());
        verify(userRegistryConnector, times(groupInfo.getMembers().size()))
                .getUserByInternalId(anyString(), eq(EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(foundGroup.getCreatedBy().getId(), EnumSet.of(name, familyName));
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(foundGroup.getModifiedBy().getId(), EnumSet.of(name, familyName));

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(partyConnector, userRegistryConnector, groupConnector);
    }

    @Test
    void getUserGroupById_nullModifiedBy() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
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

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId.get());
        User createdBy = new User();
        createdBy.setId("createdBy");
        foundGroup.setCreatedBy(createdBy);

        User createdByMock = mockInstance(new User(), "setId");

        when(groupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);
        when(userRegistryConnector.getUserByInternalId(anyString(), any()))
                .thenAnswer(invocation -> {
                    User userMock = new User();
                    userMock.setId(invocation.getArgument(0, String.class));
                    return userMock;
                });
        when(userRegistryConnector.getUserByInternalId(any(), any()))
                .thenAnswer(invocation -> {
                    createdByMock.setId(invocation.getArgument(0, String.class));
                    return createdByMock;
                });
        //when
        UserGroupInfo groupInfo = groupService.getUserGroupById(groupId, institutionId);
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
        verify(groupConnector, times(1))
                .getUserGroupById(anyString());
        verify(userRegistryConnector, times(groupInfo.getMembers().size()))
                .getUserByInternalId(anyString(), eq(EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(foundGroup.getCreatedBy().getId(), EnumSet.of(name, familyName));

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(partyConnector, userRegistryConnector, groupConnector);
    }

    @Test
    void getUserGroupById_noRelationshipMember() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
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

        when(partyConnector.getUsers(any(), any()))
                .thenReturn(List.of(userInfoMock4));
        when(userRegistryConnector.getUserByInternalId(anyString(), any()))
                .thenAnswer(invocation -> {
                    User userMock = new User();
                    userMock.setId(invocation.getArgument(0, String.class));
                    return userMock;
                });

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId.get());
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

        when(groupConnector.getUserGroupById(any()))
                .thenReturn(foundGroup);
        when(userRegistryConnector.getUserByInternalId(eq(createdById), any()))
                .thenReturn(createdByMock);
        when(userRegistryConnector.getUserByInternalId(eq(modifiedById), any()))
                .thenReturn(modifiedByMock);
        //when
        UserGroupInfo groupInfo = groupService.getUserGroupById(groupId, institutionId);
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
        verify(groupConnector, times(1))
                .getUserGroupById(groupId);
        verify(userRegistryConnector, times(groupInfo.getMembers().size()))
                .getUserByInternalId(anyString(), eq(EnumSet.of(name, familyName, workContacts)));
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(foundGroup.getCreatedBy().getId(), EnumSet.of(name, familyName));
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(foundGroup.getModifiedBy().getId(), EnumSet.of(name, familyName));

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(partyConnector, userRegistryConnector, groupConnector);
    }

    @Test
    void getUserGroupById_nullId() {
        //given
        String groupId = null;
        Optional<String> institutionId = Optional.of("institutionId");
        //when
        Executable executable = () -> groupService.getUserGroupById(groupId, institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
        Mockito.verifyNoInteractions(userRegistryConnector, partyConnector);

    }

    @Test
    void getUserGroupById_nullInstitutionId() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = null;
        //when
        Executable executable = () -> groupService.getUserGroupById(groupId, institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An optional of institutionId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
        Mockito.verifyNoInteractions(userRegistryConnector, partyConnector);

    }

    @Test
    void getUserGroupById_invalidUserGroupException() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
        UserGroupInfo foundGroup = mockInstance(new UserGroupInfo(), "setId");
        foundGroup.setId(groupId);
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        userInfoMock1.setId("createdBy");
        userInfoMock2.setId("modifiedBy");
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());

        when(groupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);
        //when
        Executable executable = () -> groupService.getUserGroupById(groupId, institutionId);
        //then
        InvalidUserGroupException e = assertThrows(InvalidUserGroupException.class, executable);
        assertEquals("Could not find a UserGroup for given institutionId", e.getMessage());
        verify(groupConnector, times(1))
                .getUserGroupById(anyString());
        verifyNoMoreInteractions(groupConnector);
        Mockito.verifyNoInteractions(userRegistryConnector, partyConnector);
    }

    @Test
    void addMemberToUserGroup() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
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
        foundGroup.setInstitutionId(institutionId.get());
        foundGroup.setProductId(productId);

        when(groupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(members);
        //when
        Executable executable = () -> groupService.addMemberToUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        verify(groupConnector, times(1))
                .addMemberToUserGroup(anyString(), any());
        verify(groupConnector, times(1))
                .getUserGroupById(groupId);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());

        verifyNoMoreInteractions(groupConnector, partyConnector);
    }

    @Test
    void addMemberToUserGroup_invalidMember() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
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
        foundGroup.setInstitutionId(institutionId.get());
        foundGroup.setProductId(productId);

        when(groupConnector.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(members);
        //when
        Executable executable = () -> groupService.addMemberToUserGroup(groupId, userId);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("This user is not allowed for this group", e.getMessage());
        verify(groupConnector, times(1))
                .getUserGroupById(groupId);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(partyConnector, times(1))
                .getUsers(eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(groupConnector, partyConnector);
    }

    @Test
    void addMemberToUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = randomUUID();
        //when
        Executable executable = () -> groupService.addMemberToUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void addMemberToUserGroup_nullUserId() {
        //given
        String groupId = "groupId";
        UUID userId = null;
        //when
        Executable executable = () -> groupService.addMemberToUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMemberFromUserGroup() {
        //given
        String groupId = "groupId";
        UUID userId = randomUUID();
        //when
        Executable executable = () -> groupService.deleteMemberFromUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        verify(groupConnector, times(1))
                .deleteMemberFromUserGroup(anyString(), any());
        verifyNoMoreInteractions(groupConnector);
    }

    @Test
    void deleteMemberFromUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = randomUUID();
        //when
        Executable executable = () -> groupService.deleteMemberFromUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMemberFromUserGroup_nullUserId() {
        //given
        String groupId = "groupId";
        UUID userId = null;
        //when
        Executable executable = () -> groupService.deleteMemberFromUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void getUserGroups() {
        //given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = Optional.of("productId");
        Optional<UUID> userId = Optional.of(randomUUID());
        Pageable pageable = PageRequest.of(1, 2);
        UserGroupInfo userGroupInfo = mockInstance(new UserGroupInfo());
        when(groupConnector.getUserGroups(any(), any()))
                .thenAnswer(invocation -> getPage(List.of(userGroupInfo), invocation.getArgument(1, Pageable.class), () -> 1L));
        //when
        Page<UserGroupInfo> groupInfos = groupService.getUserGroups(institutionId, productId, userId, pageable);
        //then
        assertNotNull(groupInfos);
        assertNotNull(groupInfos.getContent());
        assertEquals(1, groupInfos.getContent().size());
        verify(groupConnector, times(1))
                .getUserGroups(any(), any());
        verifyNoMoreInteractions(groupConnector);
    }

    @Test
    void getUserGroups_nullInstitutionId() {
        //given
        Optional<String> institutionId = null;
        Optional<String> productId = Optional.empty();
        Optional<UUID> userId = Optional.empty();
        Pageable pageable = PageRequest.of(1, 2);
        //when
        Executable executable = () -> groupService.getUserGroups(institutionId, productId, userId, pageable);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An optional institutionId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void getUserGroups_nullProductId() {
        //given
        Optional<String> institutionId = Optional.empty();
        Optional<String> productId = null;
        Optional<UUID> userId = Optional.empty();
        Pageable pageable = PageRequest.of(1, 2);
        //when
        Executable executable = () -> groupService.getUserGroups(institutionId, productId, userId, pageable);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An optional productId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void getUserGroups_nullUserId() {
        //given
        Optional<String> institutionId = Optional.empty();
        Optional<String> productId = Optional.empty();
        Optional<UUID> userId = null;
        Pageable pageable = PageRequest.of(1, 2);
        //when
        Executable executable = () -> groupService.getUserGroups(institutionId, productId, userId, pageable);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An optional userId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMembers_nullRelationshipId() {
        //given
        String relationshipId = null;
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A relationshipId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMembers_nullProductId() {
        //given
        String relationshipId = "relationshipId";
        String productId = null;
        UserInfo userInfoMock = mockInstance(new UserInfo());
        ProductInfo productInfoMock = mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        when(partyConnector.getUser(anyString()))
                .thenReturn(userInfoMock);
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A product Id is required", e.getMessage());
        verify(partyConnector, times(1))
                .getUser(relationshipId);
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMembers_nullInstitutionId() {
        //given
        String relationshipId = "relationship";
        String institutionId = null;
        String productId = "productId";
        UserInfo userInfoMock = mockInstance(new UserInfo());
        userInfoMock.setInstitutionId(institutionId);
        ProductInfo productInfoMock = mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setProductId(productId);
        filter.setUserId(userInfoMock.getId());
        when(partyConnector.getUser(anyString()))
                .thenReturn(userInfoMock);
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("An institution id is required", e.getMessage());
        verify(partyConnector, times(1))
                .getUser(relationshipId);
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMembers_nullUserId() {
        //given
        String relationshipId = "relationship";
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId");
        userInfoMock.setInstitutionId(institutionId);
        ProductInfo productInfoMock = mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        when(partyConnector.getUser(anyString()))
                .thenReturn(userInfoMock);
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A user id is required", e.getMessage());
        verify(partyConnector, times(1))
                .getUser(relationshipId);
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMembers_emptyRelationships() {
        //given
        String relationshipId = "relationship";
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId");
        userInfoMock.setInstitutionId(institutionId);
        userInfoMock.setId(userId);
        ProductInfo productInfoMock = mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setProductId(productId);
        filter.setUserId(userInfoMock.getId());
        when(partyConnector.getUser(anyString()))
                .thenReturn(userInfoMock);
        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(Collections.emptyList());
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        verify(groupConnector, times(1))
                .deleteMembers(userId, institutionId, productId);
        verify(partyConnector, times(1))
                .getUsers(institutionId, filter);
        verify(partyConnector, times(1))
                .getUser(relationshipId);
        verifyNoMoreInteractions(groupConnector, partyConnector);
    }

    @Test
    void deleteMembers() {
        //given
        String relationshipId = "relationship";
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId");
        userInfoMock.setInstitutionId(institutionId);
        userInfoMock.setId(userId);
        ProductInfo productInfoMock = mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setProductId(productId);
        filter.setUserId(userInfoMock.getId());
        when(partyConnector.getUser(anyString()))
                .thenReturn(userInfoMock);
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

        when(partyConnector.getUsers(anyString(), any()))
                .thenReturn(members);
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        verify(partyConnector, times(1))
                .getUsers(institutionId, filter);
        verify(partyConnector, times(1))
                .getUser(relationshipId);
        verifyNoMoreInteractions(partyConnector);
        Mockito.verifyNoInteractions(groupConnector);
    }
}