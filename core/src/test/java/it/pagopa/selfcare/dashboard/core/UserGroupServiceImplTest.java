package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InternalServerErrorException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static it.pagopa.selfcare.dashboard.core.UserGroupServiceImpl.REQUIRED_GROUP_ID_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserGroupServiceImplTest {

    @InjectMocks
    private UserGroupServiceImpl groupService;

    @Mock
    private UserGroupConnector groupConnector;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private UserRegistryConnector userRegistryConnector;

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

        Mockito.when(institutionService.getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.createUserGroup(userGroup);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .createUserGroup(Mockito.any());
        Mockito.verify(institutionService, Mockito.times(1))
                .getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(groupConnector);
        Mockito.verifyNoMoreInteractions(institutionService);
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

        Mockito.when(institutionService.getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.createUserGroup(userGroup);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());
        Mockito.verify(institutionService, Mockito.times(1))
                .getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(institutionService);
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

        Mockito.when(institutionService.getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.updateUserGroup(groupId, userGroup);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verify(groupConnector, Mockito.times(1))
                .updateUserGroup(Mockito.anyString(), Mockito.any());
        Mockito.verify(institutionService, Mockito.times(1))
                .getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(groupConnector);
        Mockito.verifyNoMoreInteractions(institutionService);
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

        Mockito.when(institutionService.getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4));
        //when
        Executable executable = () -> groupService.updateUserGroup(groupId, userGroup);
        //then
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());
        Mockito.verify(institutionService, Mockito.times(1))
                .getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(institutionService);
        Mockito.verifyNoMoreInteractions(groupConnector);
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

        Mockito.when(institutionService.getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
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


        User createdByMock = TestUtils.mockInstance(new User(), "setId");
        createdByMock.setId("createdBy");
        User modifiedByMock = TestUtils.mockInstance(new User(), "setId");
        modifiedByMock.setId("modifiedBy");

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
        assertEquals(createdByMock, groupInfo.getCreatedBy());
        assertEquals(foundGroup.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(modifiedByMock, groupInfo.getModifiedBy());
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(groupConnector);
        Mockito.verify(userRegistryConnector, Mockito.times(2))
                .getUserByInternalId(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRegistryConnector);
        Mockito.verify(institutionService, Mockito.times(1))
                .getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(institutionService);

    }

    @Test
    void getUserGroupById_internalError() {
        //given
        String groupId = "groupId";
        Optional<String> institutionId = Optional.of("institutionId");
        UserGroupInfo foundGroup = TestUtils.mockInstance(new UserGroupInfo(), "setId", "setInstitutionId", "setCreatedBy", "setModifiedBy");
        foundGroup.setId(groupId);
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(UUID.randomUUID().toString());
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        Mockito.when(institutionService.getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2, userInfoMock4));

        foundGroup.setMembers(members);
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId.get());

        Mockito.when(groupConnector.getUserGroupById(Mockito.anyString()))
                .thenReturn(foundGroup);

        //when
        Executable executable = () -> groupService.getUserGroupById(groupId, institutionId);
        //then
        assertThrows(InternalServerErrorException.class, executable);

        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(groupConnector);
        Mockito.verify(institutionService, Mockito.times(1))
                .getInstitutionProductUsers(Mockito.anyString(), Mockito.anyString(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(institutionService);
        Mockito.verifyNoInteractions(userRegistryConnector);
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
        assertThrows(InvalidUserGroupException.class, executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(groupConnector);
    }

    @Test
    void addMemberToUserGroup() {
        //given
        String groupId = "groupId";
        UUID userId = UUID.randomUUID();
        //when
        Executable executable = () -> groupService.addMemberToUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(groupConnector, Mockito.times(1))
                .addMemberToUserGroup(Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(groupConnector);
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
}