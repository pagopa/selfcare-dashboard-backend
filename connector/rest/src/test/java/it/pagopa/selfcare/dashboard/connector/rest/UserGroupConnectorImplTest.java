package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.*;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.CreateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UpdateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    @Captor
    private ArgumentCaptor<CreateUserGroupRequestDto> requestDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<UpdateUserGroupRequestDto> updateRequestCaptor;

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
                .createUserGroup(requestDtoArgumentCaptor.capture());
        CreateUserGroupRequestDto request = requestDtoArgumentCaptor.getValue();
        assertEquals(userGroup.getName(), request.getName());
        assertEquals(userGroup.getDescription(), request.getDescription());
        assertEquals(userGroup.getMembers(), request.getMembers());
        assertEquals(userGroup.getInstitutionId(), request.getInstitutionId());
        assertEquals(userGroup.getProductId(), request.getProductId());
        assertEquals(UserGroupStatus.ACTIVE, request.getStatus());

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
                .updateUserGroupById(Mockito.any(), updateRequestCaptor.capture());
        UpdateUserGroupRequestDto request = updateRequestCaptor.getValue();
        assertEquals(userGroup.getName(), request.getName());
        assertEquals(userGroup.getDescription(), request.getDescription());
        assertEquals(userGroup.getMembers(), request.getMembers());
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
    void getUserGroupById_nullModifiedBy() {
        //given
        String id = "id";
        UserGroupResponse response = TestUtils.mockInstance(new UserGroupResponse(), "setMembers", "setModifiedBy");
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
        assertNull(groupInfo.getModifiedBy());
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

    @Test
    void addMemberToUserGroup() {
        //given
        String groupId = "groupId";
        UUID userId = UUID.randomUUID();
        //when
        Executable executable = () -> groupConnector.addMemberToUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(restClientMock, Mockito.times(1))
                .addMemberToUserGroup(Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void addMemberToUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = UUID.randomUUID();
        //when
        Executable executable = () -> groupConnector.addMemberToUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void addMemberToUserGroup_nullUserId() {
        //given
        String groupId = "groupId";
        UUID userId = null;
        //when
        Executable executable = () -> groupConnector.addMemberToUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void deleteMemberFromUserGroup() {
        //given
        String groupId = "groupId";
        UUID userId = UUID.randomUUID();
        //when
        Executable executable = () -> groupConnector.deleteMemberFromUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(restClientMock, Mockito.times(1))
                .deleteMemberFromUserGroup(Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void deleteMemberFromUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = UUID.randomUUID();
        //when
        Executable executable = () -> groupConnector.deleteMemberFromUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void deleteMemberFromUserGroup_nullUserId() {
        //given
        String groupId = "groupId";
        UUID userId = null;
        //when
        Executable executable = () -> groupConnector.deleteMemberFromUserGroup(groupId, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserGroups() {
        //given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = Optional.of("productId");
        Optional<UUID> userId = Optional.of(UUID.randomUUID());
        UserGroupFilter filter = new UserGroupFilter();
        filter.setInstitutionId(institutionId);
        filter.setProductId(productId);
        filter.setUserId(userId);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));
        UserGroupResponse response1 = TestUtils.mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        response1.setCreatedAt(Instant.now());
        response1.setModifiedAt(Instant.now());
        Mockito.when(restClientMock.getUserGroups(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(response1));
        //when
        Collection<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertEquals(1, groupInfos.size());
        UserGroupInfo groupInfo = groupInfos.iterator().next();
        assertEquals(response1.getId(), groupInfo.getId());
        assertEquals(response1.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response1.getProductId(), groupInfo.getProductId());
        assertEquals(response1.getStatus(), groupInfo.getStatus());
        assertEquals(response1.getDescription(), groupInfo.getDescription());
        assertEquals(response1.getName(), groupInfo.getName());
        assertEquals(response1.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(Collectors.toList()));
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserGroups(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_nullResponse_emptyInstitutionId_emptyProductId_emptyUserId_unPaged() {
        //given
        UserGroupFilter filter = new UserGroupFilter();
        Pageable pageable = Pageable.unpaged();
        //when
        Collection<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertTrue(groupInfos.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserGroups(Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNotNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_notEmptyInstitutionId_notEmptyProductId() {
        //given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = Optional.of("productId");
        UserGroupFilter filter = new UserGroupFilter();
        filter.setInstitutionId(institutionId);
        filter.setProductId(productId);
        Pageable pageable = Pageable.unpaged();
        UserGroupResponse response1 = TestUtils.mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        response1.setCreatedAt(Instant.now());
        response1.setModifiedAt(Instant.now());
        Mockito.when(restClientMock.getUserGroups(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(response1));
        //when
        Collection<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertEquals(1, groupInfos.size());
        UserGroupInfo groupInfo = groupInfos.iterator().next();
        assertEquals(response1.getId(), groupInfo.getId());
        assertEquals(response1.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response1.getProductId(), groupInfo.getProductId());
        assertEquals(response1.getStatus(), groupInfo.getStatus());
        assertEquals(response1.getDescription(), groupInfo.getDescription());
        assertEquals(response1.getName(), groupInfo.getName());
        assertEquals(response1.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(Collectors.toList()));
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserGroups(Mockito.eq(institutionId.get()), Mockito.eq(productId.get()), Mockito.isNull(), Mockito.isNotNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_notEmptyUserId() {
        //given
        Optional<UUID> userId = Optional.of(UUID.randomUUID());
        UserGroupFilter filter = new UserGroupFilter();
        filter.setUserId(userId);
        Pageable pageable = Pageable.unpaged();
        UserGroupResponse response1 = TestUtils.mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        response1.setCreatedAt(Instant.now());
        response1.setModifiedAt(Instant.now());
        Mockito.when(restClientMock.getUserGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(response1));
        //when
        Collection<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertEquals(1, groupInfos.size());
        UserGroupInfo groupInfo = groupInfos.iterator().next();
        assertEquals(response1.getId(), groupInfo.getId());
        assertEquals(response1.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response1.getProductId(), groupInfo.getProductId());
        assertEquals(response1.getStatus(), groupInfo.getStatus());
        assertEquals(response1.getDescription(), groupInfo.getDescription());
        assertEquals(response1.getName(), groupInfo.getName());
        assertEquals(response1.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(Collectors.toList()));
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserGroups(Mockito.isNull(), Mockito.isNull(), Mockito.eq(userId.get()), Mockito.isNotNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_nullGroupInfos() {
        //given

        UserGroupFilter filter = new UserGroupFilter();
        Pageable pageable = Pageable.unpaged();
        Mockito.when(restClientMock.getUserGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(null);
        //when
        Collection<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertTrue(groupInfos.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserGroups(Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNotNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void groupResponse_toGroupInfo() {
        //given
        UserGroupResponse response1 = TestUtils.mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        response1.setCreatedAt(Instant.now());
        response1.setModifiedAt(Instant.now());
        //when
        UserGroupInfo groupInfo = UserGroupConnectorImpl.GROUP_RESPONSE_TO_GROUP_INFO.apply(response1);
        //then
        assertEquals(response1.getId(), groupInfo.getId());
        assertEquals(response1.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response1.getProductId(), groupInfo.getProductId());
        assertEquals(response1.getStatus(), groupInfo.getStatus());
        assertEquals(response1.getDescription(), groupInfo.getDescription());
        assertEquals(response1.getName(), groupInfo.getName());
        assertEquals(response1.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(Collectors.toList()));
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
    }

    @Test
    void groupResponse_toGroupInfo_nullMembers() {
        //given
        UserGroupResponse response1 = TestUtils.mockInstance(new UserGroupResponse(), "setMembers");
        //when
        UserGroupInfo groupInfo = UserGroupConnectorImpl.GROUP_RESPONSE_TO_GROUP_INFO.apply(response1);
        //then
        assertEquals(response1.getId(), groupInfo.getId());
        assertEquals(response1.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response1.getProductId(), groupInfo.getProductId());
        assertEquals(response1.getStatus(), groupInfo.getStatus());
        assertEquals(response1.getDescription(), groupInfo.getDescription());
        assertEquals(response1.getName(), groupInfo.getName());
        assertEquals(response1.getMembers(), groupInfo.getMembers());
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
    }

    @Test
    void deleteMembers() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> {
            groupConnector.deleteMembers(memberId.toString(), institutionId, productId);
            Thread.sleep(1000);
        };
        //when
        assertDoesNotThrow(executable);
        Mockito.verify(restClientMock, Mockito.times(1))
                .deleteMembers(memberId, institutionId, productId);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void deleteMembers_institutionId() {
        //given
        String institutionId = null;
        String productId = "productId";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> groupConnector.deleteMembers(memberId.toString(), institutionId, productId);
        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("Required institutionId", e.getMessage());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

}