package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.model.groups.*;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.CreateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UpdateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.rest.UserGroupConnectorImpl.REQUIRED_GROUP_ID_MESSAGE;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.support.PageableExecutionUtils.getPage;


@ExtendWith(MockitoExtension.class)
class UserGroupConnectorImplTest extends BaseConnectorTest {

    @Mock
    private UserGroupRestClient restClientMock;

    @InjectMocks
    private UserGroupConnectorImpl groupConnector;

    @Captor
    private ArgumentCaptor<CreateUserGroupRequestDto> requestDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<UpdateUserGroupRequestDto> updateRequestCaptor;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void createGroup_nullGroup() {

        Executable executable = () -> groupConnector.createUserGroup(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User Group is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void createGroup() throws IOException {

        ClassPathResource resource = new ClassPathResource("stubs/createUserGroup.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        CreateUserGroup userGroup = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource resourceRequest = new ClassPathResource("stubs/createUserGroupRequest.json");
        byte[] resourceStreamRequest = Files.readAllBytes(resourceRequest.getFile().toPath());
        CreateUserGroupRequestDto userGroupRequest = objectMapper.readValue(resourceStreamRequest, new TypeReference<>() {
        });

        ClassPathResource ResponseResource = new ClassPathResource("stubs/userGroupResponse.json");
        byte[] responseStream = Files.readAllBytes(ResponseResource.getFile().toPath());
        UserGroupResponse response = objectMapper.readValue(responseStream, new TypeReference<>() {
        });

        when(restClientMock.createUserGroup(userGroupRequest)).thenReturn(response);
        String groupId = groupConnector.createUserGroup(userGroup);

        assertEquals(response.getId(), groupId);
        Mockito.verify(restClientMock, times(1))
                .createUserGroup(requestDtoArgumentCaptor.capture());
    }

    @Test
    void updateGroup_nullGroup() {

        String groupId = "groupId";

        Executable executable = () -> groupConnector.updateUserGroup(groupId, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User Group is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void updateGroup_nullId() {

        UpdateUserGroup userGroup = mockInstance(new UpdateUserGroup());
        userGroup.setMembers(List.of("string1", "string2"));

        Executable executable = () -> groupConnector.updateUserGroup(null, userGroup);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void updateGroup() {

        String groupId = "groupId";
        UpdateUserGroup userGroup = mockInstance(new UpdateUserGroup());
        userGroup.setMembers(List.of("string1", "string2"));

        Executable executable = () -> groupConnector.updateUserGroup(groupId, userGroup);

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
        //when
        Executable executable = () -> groupConnector.delete(null);
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
        //when
        Executable executable = () -> groupConnector.activate(null);
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
        //when
        Executable executable = () -> groupConnector.suspend(null);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserGroupById() throws IOException {

        String id = "id";

        ClassPathResource resource = new ClassPathResource("stubs/userGroupResponseSingle.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserGroupResponse expectedResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource resourceResponse = new ClassPathResource("stubs/userGroupInfoGetUserGroupByIdSingle.json");
        byte[] resourceStreamResponse = Files.readAllBytes(resourceResponse.getFile().toPath());
        UserGroupInfo expectedGroupInfoResponse = objectMapper.readValue(resourceStreamResponse, new TypeReference<>() {
        });

        when(restClientMock.getUserGroupById(anyString())).thenReturn(expectedResponse);

        UserGroupInfo groupInfo = groupConnector.getUserGroupById(id);
        assertEquals(expectedGroupInfoResponse, groupInfo);

        verify(restClientMock, times(1)).getUserGroupById(anyString());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroupById_nullModifiedBy() throws IOException {

        String id = "id";

        ClassPathResource resource = new ClassPathResource("stubs/userGroupResponseNullModifiedBy.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserGroupResponse expectedResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource resourceResponse = new ClassPathResource("stubs/userGroupInfoNullModifiedBy.json");
        byte[] resourceStreamResponse = Files.readAllBytes(resourceResponse.getFile().toPath());
        UserGroupInfo expectedGroupInfo = objectMapper.readValue(resourceStreamResponse, new TypeReference<>() {
        });

        when(restClientMock.getUserGroupById(anyString())).thenReturn(expectedResponse);

        UserGroupInfo groupInfo = groupConnector.getUserGroupById(id);
        assertEquals(expectedGroupInfo, groupInfo);
        verify(restClientMock, times(1)).getUserGroupById(anyString());
    }

    @Test
    void getUserGroupById_nullId() {
        //when
        Executable executable = () -> groupConnector.getUserGroupById(null);
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
        UUID userId = randomUUID();
        //when
        Executable executable = () -> groupConnector.addMemberToUserGroup(null, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void addMemberToUserGroup_nullUserId() {
        //given
        String groupId = "groupId";
        //when
        Executable executable = () -> groupConnector.addMemberToUserGroup(groupId, null);
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
        UUID userId = randomUUID();
        //when
        Executable executable = () -> groupConnector.deleteMemberFromUserGroup(null, userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void deleteMemberFromUserGroup_nullUserId() {
        //given
        String groupId = "groupId";
        //when
        Executable executable = () -> groupConnector.deleteMemberFromUserGroup(groupId, null);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserGroups() throws IOException {
        // Given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = Optional.of("productId");
        Optional<UUID> userId = Optional.of(randomUUID());
        UserGroupFilter filter = new UserGroupFilter();
        filter.setInstitutionId(institutionId);
        filter.setProductId(productId);
        filter.setUserId(userId);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));

        ClassPathResource resource = new ClassPathResource("stubs/userGroupInfoGetUserGroups.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserGroupInfo userGroupInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource ResponseResource = new ClassPathResource("stubs/userGroupResponse.json");
        byte[] responseStream = Files.readAllBytes(ResponseResource.getFile().toPath());
        UserGroupResponse response = objectMapper.readValue(responseStream, new TypeReference<>() {
        });


        when(restClientMock.getUserGroups(filter.getInstitutionId().orElse(null),
                filter.getProductId().orElse(null),
                filter.getUserId().orElse(null),
                List.of(UserGroupStatus.ACTIVE, UserGroupStatus.SUSPENDED),
                pageable))
                .thenReturn(new PageImpl<>(List.of(response)));

        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);

        assertEquals(1, groupInfos.getTotalElements());
        assertEquals(userGroupInfo, groupInfos.getContent().get(0));
        Mockito.verify(restClientMock, times(1))
                .getUserGroups(filter.getInstitutionId().orElse(null),
                        filter.getProductId().orElse(null),
                        filter.getUserId().orElse(null),
                        List.of(UserGroupStatus.ACTIVE, UserGroupStatus.SUSPENDED),
                        pageable);

        UserGroupInfo actualUserGroupInfo = groupInfos.getContent().get(0);
        assertEquals(userGroupInfo, actualUserGroupInfo);

    }

    @Test
    void getUserGroups_nullResponse_emptyInstitutionId_emptyProductId_emptyUserId_unPaged() {
        //given
        UserGroupFilter filter = new UserGroupFilter();
        Pageable pageable = Pageable.unpaged();
        when(restClientMock.getUserGroups(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> getPage(emptyList(), invocation.getArgument(4, Pageable.class), () -> 0L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertTrue(groupInfos.isEmpty());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_notEmptyInstitutionId_notEmptyProductId() throws IOException {
        //given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = Optional.of("productId");
        UserGroupFilter filter = new UserGroupFilter();
        filter.setInstitutionId(institutionId);
        filter.setProductId(productId);
        Pageable pageable = Pageable.unpaged();
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of("123", "321"));
        response1.setCreatedAt(Instant.MAX);
        response1.setModifiedAt(Instant.MAX);
        when(restClientMock.getUserGroups(anyString(), anyString(), any(), any(), any()))
                .thenAnswer(invocation -> getPage(List.of(response1), invocation.getArgument(4, Pageable.class), () -> 1L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);

        ClassPathResource resource = new ClassPathResource("stubs/getUserGroups_notEmptyInstitutionId_notEmptyProductId_notEmptyUserId.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        String expectedUserGroupInfoJson = new String(resourceStream, StandardCharsets.UTF_8);

        UserGroupInfo expectedUserGroupInfo = objectMapper.readValue(expectedUserGroupInfoJson, UserGroupInfo.class);

        //then
        assertEquals(groupInfos.getContent().get(0), expectedUserGroupInfo);
        verify(restClientMock, times(1))
                .getUserGroups(eq(institutionId.get()), eq(productId.get()), isNull(), eq(List.of(UserGroupStatus.ACTIVE, UserGroupStatus.SUSPENDED)), isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_notEmptyUserId() throws IOException {
        //given
        Optional<UUID> userId = Optional.of(randomUUID());
        UserGroupFilter filter = new UserGroupFilter();
        filter.setUserId(userId);
        Pageable pageable = Pageable.unpaged();
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of("123", "321"));
        response1.setCreatedAt(Instant.MAX);
        response1.setModifiedAt(Instant.MAX);
        when(restClientMock.getUserGroups(any(), any(), any(), anyList(), any()))
                .thenAnswer(invocation -> getPage(List.of(response1), invocation.getArgument(4, Pageable.class), () -> 1L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then

        ClassPathResource resource = new ClassPathResource("stubs/getUserGroups_notEmptyInstitutionId_notEmptyProductId_notEmptyUserId.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        String expectedUserGroupInfoJson = new String(resourceStream, StandardCharsets.UTF_8);

        UserGroupInfo expectedUserGroupInfo = objectMapper.readValue(expectedUserGroupInfoJson, UserGroupInfo.class);
        assertEquals(groupInfos.getContent().get(0), expectedUserGroupInfo);

        verify(restClientMock, times(1))
                .getUserGroups(isNull(), isNull(), eq(userId.get()), eq(List.of(UserGroupStatus.ACTIVE, UserGroupStatus.SUSPENDED)), isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroups_nullGroupInfos() {
        //given

        UserGroupFilter filter = new UserGroupFilter();
        Pageable pageable = Pageable.unpaged();
        when(restClientMock.getUserGroups(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> getPage(emptyList(), invocation.getArgument(4, Pageable.class), () -> 0L));
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);
        //then
        assertNotNull(groupInfos);
        assertTrue(groupInfos.isEmpty());
        verify(restClientMock, times(1))
                .getUserGroups(isNull(), isNull(), isNull(), eq(List.of(UserGroupStatus.ACTIVE, UserGroupStatus.SUSPENDED)), isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void groupResponse_toGroupInfo() throws IOException {
        //given
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
        response1.setMembers(List.of("123", "321"));
        response1.setCreatedAt(Instant.MAX);
        response1.setModifiedAt(Instant.MAX);
        //when
        UserGroupInfo groupInfo = UserGroupConnectorImpl.GROUP_RESPONSE_TO_GROUP_INFO.apply(response1);
        //then
        ClassPathResource resource = new ClassPathResource("stubs/groupResponse_toGroupInfo.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        String expectedUserGroupInfoJson = new String(resourceStream, StandardCharsets.UTF_8);

        UserGroupInfo expectedUserGroupInfo = objectMapper.readValue(expectedUserGroupInfoJson, UserGroupInfo.class);
        assertEquals(groupInfo, expectedUserGroupInfo);
    }

    @Test
    void groupResponse_toGroupInfo_nullMembers() throws IOException {
        //given
        UserGroupResponse response1 = mockInstance(new UserGroupResponse(), "setMembers");
        response1.setCreatedAt(Instant.MAX);
        response1.setModifiedAt(Instant.MAX);
        //when
        UserGroupInfo groupInfo = UserGroupConnectorImpl.GROUP_RESPONSE_TO_GROUP_INFO.apply(response1);
        //then
        ClassPathResource resource = new ClassPathResource("stubs/groupResponse_toGroupInfo_nullMembers.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        String expectedUserGroupInfoJson = new String(resourceStream, StandardCharsets.UTF_8);

        UserGroupInfo expectedUserGroupInfo = objectMapper.readValue(expectedUserGroupInfoJson, UserGroupInfo.class);
        assertEquals(groupInfo, expectedUserGroupInfo);
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
        String productId = "productId";
        UUID memberId = randomUUID();
        //when
        Executable executable = () -> groupConnector.deleteMembers(memberId.toString(), null, productId);
        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("Required institutionId", e.getMessage());
        verifyNoMoreInteractions(restClientMock);
    }

}