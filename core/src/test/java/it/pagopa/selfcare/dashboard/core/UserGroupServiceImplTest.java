package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserResource;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;

import static it.pagopa.selfcare.dashboard.core.UserGroupServiceImpl.REQUIRED_GROUP_ID_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserGroupServiceImpl.class, CoreTestConfig.class})
class UserGroupServiceImplTest {

    @Autowired
    private UserGroupService groupService;

    @MockBean
    private UserGroupConnector groupConnector;

    @MockBean
    private PartyConnector partyConnector;

    @MockBean
    private UserRegistryConnector userRegistryConnector;

    @Captor
    private ArgumentCaptor<Throwable> throwableCaptor;

    @SpyBean
    private SimpleAsyncUncaughtExceptionHandler simpleAsyncUncaughtExceptionHandler;

    @Test
    void createGroup() {
        //given
        CreateUserGroup userGroup = TestUtils.mockInstance(new CreateUserGroup());
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        List<String> userIds = List.of(id1, id2, id3, id4);
        userGroup.setMembers(userIds);
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.createUserGroup(userGroup);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .createUserGroup(Mockito.any());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(userGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verifyNoMoreInteractions(groupConnector, partyConnector);
    }

    @Test
    void createGroup_invalidList() {
        //given
        CreateUserGroup userGroup = TestUtils.mockInstance(new CreateUserGroup());
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        List<String> userIds = List.of(UUID.randomUUID().toString(), id2, id3, id4);
        userGroup.setMembers(userIds);
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.createUserGroup(userGroup);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(userGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verifyNoMoreInteractions(partyConnector);
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void delete() {
        // given
        String groupId = "relationshipId";
        // when
        groupService.delete(groupId);
        // then
        Mockito.verify(groupConnector, Mockito.times(1))
                .delete(groupId);
        Mockito.verifyNoMoreInteractions(groupConnector);
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
        Mockito.verify(groupConnector, Mockito.times(1))
                .activate(groupId);
        Mockito.verifyNoMoreInteractions(groupConnector);
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
        Mockito.verify(groupConnector, Mockito.times(1))
                .suspend(groupId);
        Mockito.verifyNoMoreInteractions(groupConnector);
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
        UpdateUserGroup userGroup = TestUtils.mockInstance(new UpdateUserGroup());
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        List<String> userIds = List.of(id1.toString(), id2, id3, id4);
        userGroup.setMembers(userIds);

        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId");
        foundGroup.setId(groupId);
        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);

        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.updateUserGroup(groupId, userGroup);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(foundGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verify(groupConnector, Mockito.times(1))
                .updateUserGroup(Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(partyConnector, groupConnector);
    }

    @Test
    void updateUserGroup_invalidMembersList() {
        //given
        String groupId = "groupId";
        UpdateUserGroup userGroup = TestUtils.mockInstance(new UpdateUserGroup());
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        List<String> userIds = List.of(UUID.randomUUID().toString(), id2, id3, id4);
        userGroup.setMembers(userIds);

        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId");
        foundGroup.setId(groupId);
        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);

        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.updateUserGroup(groupId, userGroup);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(foundGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(partyConnector, groupConnector);
    }

    @Test
    void getUserGroupById() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId", "setInstitutionId", "setCreatedBy", "setModifiedBy");
        foundGroup.setId(groupId);
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId.get());
        UserResource createdBy = new UserResource();
        createdBy.setId("createdBy");
        foundGroup.setCreatedBy(createdBy);
        UserResource modifiedBy = new UserResource();
        modifiedBy.setId("modifiedBy");
        foundGroup.setModifiedBy(modifiedBy);


        UserResource createdByMock = TestUtils.mockInstance(new UserResource(), "setId");
        createdByMock.setId(UUID.randomUUID().toString());
        UserResource modifiedByMock = TestUtils.mockInstance(new UserResource(), "setId");
        modifiedByMock.setId(UUID.randomUUID().toString());

        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);
        Mockito.when(userRegistryConnector.getUserByInternalId(foundGroup.getCreatedBy().getId()))
                .thenReturn(createdByMock);
        Mockito.when(userRegistryConnector.getUserByInternalId(foundGroup.getModifiedBy().getId()))
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
        assertEquals(foundGroup.getMembers(), groupInfo.getMembers());
        assertEquals(foundGroup.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(createdByMock.getId().toString(), groupInfo.getCreatedBy().getId());
        assertEquals(foundGroup.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(modifiedByMock.getId().toString(), groupInfo.getModifiedBy().getId());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verify(userRegistryConnector, Mockito.times(2))
                .getUserByInternalId(Mockito.anyString());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verifyNoMoreInteractions(partyConnector, userRegistryConnector, groupConnector);
    }

    @Test
    void getUserGroupById_nullModifiedBy() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId", "setInstitutionId", "setCreatedBy", "setModifiedBy");
        foundGroup.setId(groupId);
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId.get());
        UserResource createdBy = new UserResource();
        createdBy.setId("createdBy");
        foundGroup.setCreatedBy(createdBy);


        UserResource createdByMock = TestUtils.mockInstance(new UserResource(), "setId");
        createdByMock.setId(UUID.randomUUID().toString());

        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);
        Mockito.when(userRegistryConnector.getUserByInternalId(foundGroup.getCreatedBy().getId()))
                .thenReturn(createdByMock);
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
        assertEquals(createdByMock.getId().toString(), groupInfo.getCreatedBy().getId());
        assertEquals(foundGroup.getModifiedAt(), groupInfo.getModifiedAt());
        assertNull(groupInfo.getModifiedBy());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verify(userRegistryConnector, Mockito.times(1))
                .getUserByInternalId(Mockito.anyString());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verifyNoMoreInteractions(partyConnector, userRegistryConnector, groupConnector);
    }

    @Test
    void getUserGroupById_noRelationshipMember() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId", "setInstitutionId", "setCreatedBy", "setModifiedBy");
        foundGroup.setId(groupId);
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of(userInfoMock4));

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId.get());
        UserResource createdBy = new UserResource();
        createdBy.setId("createdBy");
        foundGroup.setCreatedBy(createdBy);
        UserResource modifiedBy = new UserResource();
        modifiedBy.setId("modifiedBy");
        foundGroup.setModifiedBy(modifiedBy);


        UserResource createdByMock = TestUtils.mockInstance(new UserResource(), "setId");
        createdByMock.setId(UUID.randomUUID().toString());
        UserResource modifiedByMock = TestUtils.mockInstance(new UserResource(), "setId");
        modifiedByMock.setId(UUID.randomUUID().toString());

        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);
        Mockito.when(userRegistryConnector.getUserByInternalId(foundGroup.getCreatedBy().getId()))
                .thenReturn(createdByMock);
        Mockito.when(userRegistryConnector.getUserByInternalId(foundGroup.getModifiedBy().getId()))
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
        assertEquals(createdByMock.getId().toString(), groupInfo.getCreatedBy().getId());
        assertEquals(foundGroup.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(modifiedByMock.getId().toString(), groupInfo.getModifiedBy().getId());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verify(userRegistryConnector, Mockito.times(2))
                .getUserByInternalId(Mockito.anyString());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verifyNoMoreInteractions(partyConnector, userRegistryConnector, groupConnector);
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
        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId");
        foundGroup.setId(groupId);
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        UUID id4 = UUID.randomUUID();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        userInfoMock1.setId("createdBy");
        userInfoMock2.setId("modifiedBy");
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());

        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);
        //when
        Executable executable = () -> groupService.getUserGroupById(groupId, institutionId);
        //then
        InvalidUserGroupException e = assertThrows(InvalidUserGroupException.class, executable);
        assertEquals("Could not find a UserGroup for given institutionId", e.getMessage());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(groupConnector);
        Mockito.verifyNoInteractions(userRegistryConnector, partyConnector);
    }

    @Test
    void addMemberToUserGroup() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
        String productId = "productId";
        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId", "setInstitutionId");
        foundGroup.setId(groupId);
        UUID userId = UUID.randomUUID();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

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

        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(members);
        //when
        Executable executable = () -> groupService.addMemberToUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .addMemberToUserGroup(Mockito.anyString(), Mockito.any());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(groupId);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());

        Mockito.verifyNoMoreInteractions(groupConnector, partyConnector);
    }

    @Test
    void addMemberToUserGroup_invalidMember() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
        String productId = "productId";
        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId", "setInstitutionId");
        foundGroup.setId(groupId);
        UUID userId = UUID.randomUUID();
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

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

        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(members);
        //when
        Executable executable = () -> groupService.addMemberToUserGroup(groupId, userId);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("This user is not allowed for this group", e.getMessage());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(groupId);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId.get()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId().get());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verifyNoMoreInteractions(groupConnector, partyConnector);
    }

    @Test
    void addMemberToUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = UUID.randomUUID();
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
        UUID userId = UUID.randomUUID();
        //when
        Executable executable = () -> groupService.deleteMemberFromUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .deleteMemberFromUserGroup(Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(groupConnector);
    }

    @Test
    void deleteMemberFromUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = UUID.randomUUID();
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
        Optional<UUID> userId = Optional.of(UUID.randomUUID());
        Pageable pageable = PageRequest.of(1, 2);
        UserGroupInfo userGroupInfo = TestUtils.mockInstance(new UserGroupInfo());
        Mockito.when(groupConnector.getUserGroups(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userGroupInfo));
        //when
        Collection<UserGroupInfo> groupInfos = groupService.getUserGroups(institutionId, productId, userId, pageable);
        //then
        assertNotNull(groupInfos);
        assertEquals(1, groupInfos.size());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroups(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(groupConnector);
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
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
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
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo());
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        Mockito.when(partyConnector.getUser(Mockito.anyString()))
                .thenReturn(userInfoMock);
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A product Id is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMembers_nullInstitutionId() {
        //given
        String relationshipId = "relationship";
        String institutionId = null;
        String productId = "productId";
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo());
        userInfoMock.setInstitutionId(institutionId);
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setProductId(Optional.of(productId));
        filter.setUserId(Optional.of(userInfoMock.getId()));
        Mockito.when(partyConnector.getUser(Mockito.anyString()))
                .thenReturn(userInfoMock);
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("An institution id is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verifyNoInteractions(groupConnector);
    }

    @Test
    void deleteMembers_nullUserId() {
        //given
        String relationshipId = "relationship";
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setId");
        userInfoMock.setInstitutionId(institutionId);
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        Mockito.when(partyConnector.getUser(Mockito.anyString()))
                .thenReturn(userInfoMock);
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A user id is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
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
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setId");
        userInfoMock.setInstitutionId(institutionId);
        userInfoMock.setId(userId);
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setProductId(Optional.of(productId));
        filter.setUserId(Optional.of(userInfoMock.getId()));
        Mockito.when(partyConnector.getUser(Mockito.anyString()))
                .thenReturn(userInfoMock);
        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .deleteMembers(userId, institutionId, productId);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(institutionId, filter);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verifyNoMoreInteractions(groupConnector, partyConnector);
    }

    @Test
    void deleteMembers() {
        //given
        String relationshipId = "relationship";
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setId");
        userInfoMock.setInstitutionId(institutionId);
        userInfoMock.setId(userId);
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setProductId(Optional.of(productId));
        filter.setUserId(Optional.of(userInfoMock.getId()));
        Mockito.when(partyConnector.getUser(Mockito.anyString()))
                .thenReturn(userInfoMock);
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        Mockito.when(partyConnector.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(members);
        //when
        Executable executable = () -> {
            groupService.deleteMembersByRelationshipId(relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUsers(institutionId, filter);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verifyNoMoreInteractions(partyConnector);
        Mockito.verifyNoInteractions(groupConnector);
    }
}