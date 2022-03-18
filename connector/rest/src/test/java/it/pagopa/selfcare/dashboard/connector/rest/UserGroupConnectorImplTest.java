package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.rest.UserGroupConnectorImpl.REQUIRED_GROUP_ID_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class UserGroupConnectorImplTest {

    @Mock
    private UserGroupRestClient restClientMock;

    @InjectMocks
    private UserGroupConnectorImpl groupConnector;

    @Test
    void createGroup_nullGroup() {
        //given
        CreateUserGroup userGroup = null;
        //when
        Executable executable = () -> groupConnector.createUserGroup(userGroup);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User Group is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void createGroup() {
        //given
        CreateUserGroup userGroup = TestUtils.mockInstance(new CreateUserGroup());
        userGroup.setMembers(List.of("string1", "string2"));
        //when
        Executable executable = () -> groupConnector.createUserGroup(userGroup);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(restClientMock, Mockito.times(1))
                .createUserGroup(Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void updateGroup_nullGroup() {
        //given
        String groupId = "groupId";
        UpdateUserGroup userGroup = null;
        //when
        Executable executable = () -> groupConnector.updateUserGroup(groupId, userGroup);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User Group is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void updateGroup_nullId() {
        //given
        String groupId = null;
        UpdateUserGroup userGroup = TestUtils.mockInstance(new UpdateUserGroup());
        userGroup.setMembers(List.of("string1", "string2"));
        //when
        Executable executable = () -> groupConnector.updateUserGroup(groupId, userGroup);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void updateGroup() {
        //given
        String groupId = "groupId";
        UpdateUserGroup userGroup = TestUtils.mockInstance(new UpdateUserGroup());
        userGroup.setMembers(List.of("string1", "string2"));
        //when
        Executable executable = () -> groupConnector.updateUserGroup(groupId, userGroup);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(restClientMock, Mockito.times(1))
                .updateUserGroupById(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void delete() {
        //given
        String groupId = "groupId";
        //when
        groupConnector.delete(groupId);
        //then
        Mockito.verify(restClientMock, Mockito.times(1))
                .deleteUserGroupById(groupId);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void delete_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> groupConnector.delete(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void activate() {
        //given
        String groupId = "groupId";
        //when
        groupConnector.activate(groupId);
        //then
        Mockito.verify(restClientMock, Mockito.times(1))
                .activateUserGroupById(groupId);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void activate_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> groupConnector.activate(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void suspend() {
        //given
        String groupId = "groupId";
        //when
        groupConnector.suspend(groupId);
        //then
        Mockito.verify(restClientMock, Mockito.times(1))
                .suspendUserGroupById(groupId);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void suspend_nullGroupId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> groupConnector.suspend(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserGroupById() {
        //given
        String id = "id";
        UserGroupResponse response = TestUtils.mockInstance(new UserGroupResponse(), "setMembers");
        response.setMembers(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        response.setCreatedAt(Instant.now());
        response.setModifiedAt(Instant.now());
        Mockito.when(restClientMock.getUserGroupById(Mockito.anyString()))
                .thenReturn(response);
        //when
        UserGroupInfo groupInfo = groupConnector.getUserGroupById(id);
        //then
        assertEquals(response.getId(), groupInfo.getId());
        assertEquals(response.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response.getProductId(), groupInfo.getProductId());
        assertEquals(response.getStatus(), groupInfo.getStatus());
        assertEquals(response.getDescription(), groupInfo.getDescription());
        assertEquals(response.getName(), groupInfo.getName());
        assertEquals(response.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(Collectors.toList()));
        assertEquals(response.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response.getModifiedBy(), groupInfo.getModifiedBy().getId());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserGroupById(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroupById_nullId() {
        //given
        String id = null;
        //when
        Executable executable = () -> groupConnector.getUserGroupById(id);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);

    }
}