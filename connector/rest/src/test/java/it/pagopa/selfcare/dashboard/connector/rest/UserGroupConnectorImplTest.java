package it.pagopa.selfcare.dashboard.connector.rest;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.rest.UserGroupConnectorImpl.REQUIRED_GROUP_ID_MESSAGE;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.support.PageableExecutionUtils.getPage;


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
        CreateUserGroup userGroup = mockInstance(new CreateUserGroup());
        userGroup.setMembers(List.of("string1", "string2"));
        UserGroupResponse response = mockInstance(new UserGroupResponse());
        when(restClientMock.createUserGroup(any()))
                .thenReturn(response);
        //when
        String groupId = groupConnector.createUserGroup(userGroup);
        //then
        verify(restClientMock, times(1))
                .createUserGroup(requestDtoArgumentCaptor.capture());
        CreateUserGroupRequestDto request = requestDtoArgumentCaptor.getValue();
        assertEquals(userGroup.getName(), request.getName());
        assertEquals(userGroup.getDescription(), request.getDescription());
        assertEquals(userGroup.getMembers(), request.getMembers());
        assertEquals(userGroup.getInstitutionId(), request.getInstitutionId());
        assertEquals(userGroup.getProductId(), request.getProductId());
        assertEquals(UserGroupStatus.ACTIVE, request.getStatus());
        assertEquals(response.getId(), groupId);

        verifyNoMoreInteractions(restClientMock);
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
        UpdateUserGroup userGroup = mockInstance(new UpdateUserGroup());
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
        UpdateUserGroup userGroup = mockInstance(new UpdateUserGroup());
        userGroup.setMembers(List.of("string1", "string2"));
        //when
        Executable executable = () -> groupConnector.updateUserGroup(groupId, userGroup);
        //then
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .updateUserGroupById(any(), updateRequestCaptor.capture());
        UpdateUserGroupRequestDto request = updateRequestCaptor.getValue();
        assertEquals(userGroup.getName(), request.getName());
        assertEquals(userGroup.getDescription(), request.getDescription());
        assertEquals(userGroup.getMembers(), request.getMembers());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void delete() {
        //given
        String groupId = "groupId";
        //when
        groupConnector.delete(groupId);
        //then
        verify(restClientMock, times(1))
                .deleteUserGroupById(groupId);
        verifyNoMoreInteractions(restClientMock);
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
        verify(restClientMock, times(1))
                .activateUserGroupById(groupId);
        verifyNoMoreInteractions(restClientMock);
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
        verify(restClientMock, times(1))
                .suspendUserGroupById(groupId);
        verifyNoMoreInteractions(restClientMock);
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
        UserGroupResponse response = mockInstance(new UserGroupResponse(), "setMembers");
        response.setMembers(List.of(randomUUID().toString(), randomUUID().toString()));
        response.setCreatedAt(Instant.now());
        response.setModifiedAt(Instant.now());
        when(restClientMock.getUserGroupById(anyString()))
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
        assertEquals(response.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(toList()));
        assertEquals(response.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response.getModifiedBy(), groupInfo.getModifiedBy().getId());
        verify(restClientMock, times(1))
                .getUserGroupById(anyString());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroupById_nullModifiedBy() {
        //given
        String id = "id";
        UserGroupResponse response = mockInstance(new UserGroupResponse(), "setMembers", "setModifiedBy");
        response.setMembers(List.of(randomUUID().toString(), randomUUID().toString()));
        response.setCreatedAt(Instant.now());
        response.setModifiedAt(Instant.now());
        when(restClientMock.getUserGroupById(anyString()))
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
        assertEquals(response.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(toList()));
        assertEquals(response.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response.getModifiedAt(), groupInfo.getModifiedAt());
        assertNull(groupInfo.getModifiedBy());
        verify(restClientMock, times(1))
                .getUserGroupById(anyString());
        verifyNoMoreInteractions(restClientMock);
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
        UUID userId = randomUUID();
        //when
        Executable executable = () -> groupConnector.addMemberToUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .addMemberToUserGroup(anyString(), any());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void addMemberToUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = randomUUID();
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
        UUID userId = randomUUID();
        //when
        Executable executable = () -> groupConnector.deleteMemberFromUserGroup(groupId, userId);
        //then
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .deleteMemberFromUserGroup(anyString(), any());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void deleteMemberFromUserGroup_nullId() {
        //given
        String groupId = null;
        UUID userId = randomUUID();
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
        Optional<UUID> userId = Optional.of(randomUUID());
        UserGroupFilter filter = new UserGroupFilter();
        filter.setInstitutionId(institutionId);
        filter.setProductId(productId);
        filter.setUserId(userId);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of(randomUUID().toString(), randomUUID().toString()));
        response1.setCreatedAt(Instant.now());
        response1.setModifiedAt(Instant.now());
        when(restClientMock.getUserGroups(anyString(), anyString(), any(), any()))
                .thenAnswer(invocation -> getPage(List.of(response1), invocation.getArgument(3, Pageable.class), () -> 1L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertNotNull(groupInfos.getContent());
        assertEquals(1, groupInfos.getContent().size());
        UserGroupInfo groupInfo = groupInfos.iterator().next();
        assertEquals(response1.getId(), groupInfo.getId());
        assertEquals(response1.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response1.getProductId(), groupInfo.getProductId());
        assertEquals(response1.getStatus(), groupInfo.getStatus());
        assertEquals(response1.getDescription(), groupInfo.getDescription());
        assertEquals(response1.getName(), groupInfo.getName());
        assertEquals(response1.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(toList()));
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
        verify(restClientMock, times(1))
                .getUserGroups(anyString(), anyString(), any(), any());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_nullResponse_emptyInstitutionId_emptyProductId_emptyUserId_unPaged() {
        //given
        UserGroupFilter filter = new UserGroupFilter();
        Pageable pageable = Pageable.unpaged();
        when(restClientMock.getUserGroups(any(), any(), any(), any()))
                .thenAnswer(invocation -> getPage(emptyList(), invocation.getArgument(3, Pageable.class), () -> 0L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertTrue(groupInfos.isEmpty());
        verify(restClientMock, times(1))
                .getUserGroups(isNull(), isNull(), isNull(), isNotNull());
        verifyNoMoreInteractions(restClientMock);
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
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of(randomUUID().toString(), randomUUID().toString()));
        response1.setCreatedAt(Instant.now());
        response1.setModifiedAt(Instant.now());
        when(restClientMock.getUserGroups(anyString(), anyString(), any(), any()))
                .thenAnswer(invocation -> getPage(List.of(response1), invocation.getArgument(3, Pageable.class), () -> 1L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertNotNull(groupInfos.getContent());
        assertEquals(1, groupInfos.getContent().size());
        UserGroupInfo groupInfo = groupInfos.iterator().next();
        assertEquals(response1.getId(), groupInfo.getId());
        assertEquals(response1.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response1.getProductId(), groupInfo.getProductId());
        assertEquals(response1.getStatus(), groupInfo.getStatus());
        assertEquals(response1.getDescription(), groupInfo.getDescription());
        assertEquals(response1.getName(), groupInfo.getName());
        assertEquals(response1.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(toList()));
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
        verify(restClientMock, times(1))
                .getUserGroups(eq(institutionId.get()), eq(productId.get()), isNull(), isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_notEmptyUserId() {
        //given
        Optional<UUID> userId = Optional.of(randomUUID());
        UserGroupFilter filter = new UserGroupFilter();
        filter.setUserId(userId);
        Pageable pageable = Pageable.unpaged();
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of(randomUUID().toString(), randomUUID().toString()));
        response1.setCreatedAt(Instant.now());
        response1.setModifiedAt(Instant.now());
        when(restClientMock.getUserGroups(any(), any(), any(), any()))
                .thenAnswer(invocation -> getPage(List.of(response1), invocation.getArgument(3, Pageable.class), () -> 1L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertNotNull(groupInfos.getContent());
        assertEquals(1, groupInfos.getContent().size());
        UserGroupInfo groupInfo = groupInfos.iterator().next();
        assertEquals(response1.getId(), groupInfo.getId());
        assertEquals(response1.getInstitutionId(), groupInfo.getInstitutionId());
        assertEquals(response1.getProductId(), groupInfo.getProductId());
        assertEquals(response1.getStatus(), groupInfo.getStatus());
        assertEquals(response1.getDescription(), groupInfo.getDescription());
        assertEquals(response1.getName(), groupInfo.getName());
        assertEquals(response1.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(toList()));
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
        verify(restClientMock, times(1))
                .getUserGroups(isNull(), isNull(), eq(userId.get()), isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_nullGroupInfos() {
        //given

        UserGroupFilter filter = new UserGroupFilter();
        Pageable pageable = Pageable.unpaged();
        when(restClientMock.getUserGroups(any(), any(), any(), any()))
                .thenAnswer(invocation -> getPage(emptyList(), invocation.getArgument(3, Pageable.class), () -> 0L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertTrue(groupInfos.isEmpty());
        verify(restClientMock, times(1))
                .getUserGroups(isNull(), isNull(), isNull(), isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void groupResponse_toGroupInfo() {
        //given
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of(randomUUID().toString(), randomUUID().toString()));
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
        assertEquals(response1.getMembers(), groupInfo.getMembers().stream().map(UserInfo::getId).collect(toList()));
        assertEquals(response1.getCreatedAt(), groupInfo.getCreatedAt());
        assertEquals(response1.getCreatedBy(), groupInfo.getCreatedBy().getId());
        assertEquals(response1.getModifiedAt(), groupInfo.getModifiedAt());
        assertEquals(response1.getModifiedBy(), groupInfo.getModifiedBy().getId());
    }

    @Test
    void groupResponse_toGroupInfo_nullMembers() {
        //given
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
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
        UUID memberId = randomUUID();
        //when
        Executable executable = () -> {
            groupConnector.deleteMembers(memberId.toString(), institutionId, productId);
            Thread.sleep(1000);
        };
        //when
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .deleteMembers(memberId, institutionId, productId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void deleteMembers_institutionId() {
        //given
        String institutionId = null;
        String productId = "productId";
        UUID memberId = randomUUID();
        //when
        Executable executable = () -> groupConnector.deleteMembers(memberId.toString(), institutionId, productId);
        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("Required institutionId", e.getMessage());
        verifyNoMoreInteractions(restClientMock);
    }

}