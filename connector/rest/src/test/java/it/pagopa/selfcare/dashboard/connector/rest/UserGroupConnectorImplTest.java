package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.model.groups.*;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.GroupMapper;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.CreateUserGroupDto;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.PageOfUserGroupResource;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UpdateUserGroupDto;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.rest.UserGroupConnectorImpl.REQUIRED_GROUP_ID_MESSAGE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserGroupConnectorImplTest extends BaseConnectorTest {

    @Mock
    private UserGroupRestClient restClientMock;

    @InjectMocks
    private UserGroupConnectorImpl groupConnector;

    @Spy
    private GroupMapper groupMapper;

    @Captor
    private ArgumentCaptor<CreateUserGroupDto> requestDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<UpdateUserGroupDto> updateRequestCaptor;

    @BeforeEach
    public void init() {
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

        ClassPathResource userGroupResource = new ClassPathResource("stubs/createUserGroup.json");
        CreateUserGroup userGroup = objectMapper.readValue(userGroupResource.getInputStream(), new TypeReference<>() {
        });
        ClassPathResource requestResource = new ClassPathResource("stubs/createUserGroupRequest.json");
        CreateUserGroupDto expectedRequest = objectMapper.readValue(requestResource.getInputStream(), new TypeReference<>() {
        });
        ClassPathResource responseResource = new ClassPathResource("stubs/userGroupResponse.json");
        UserGroupResource mockedResponse = objectMapper.readValue(responseResource.getInputStream(), new TypeReference<>() {
        });

        when(groupMapper.toCreateUserGroupDto(any(CreateUserGroup.class))).thenReturn(expectedRequest);
        when(restClientMock._createGroupUsingPOST(any(CreateUserGroupDto.class))).thenReturn(ResponseEntity.of(Optional.of(mockedResponse)));

        String groupId = groupConnector.createUserGroup(userGroup);

        verify(groupMapper, times(1)).toCreateUserGroupDto(refEq(userGroup));
        verify(restClientMock, times(1))._createGroupUsingPOST(requestDtoArgumentCaptor.capture());
        CreateUserGroupDto capturedRequest = requestDtoArgumentCaptor.getValue();
        assertNotNull(capturedRequest);
        assertEquals(expectedRequest, capturedRequest);
        assertEquals(mockedResponse.getId(), groupId);
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
        // Arrange
        String groupId = "groupId";
        UpdateUserGroup userGroup = mockInstance(new UpdateUserGroup());
        userGroup.setMembers(List.of("895b4af3-7fa7-4442-8a0d-a2b05c6c719f", "895b4af3-7fa7-4442-8a0d-a2b05c6c719c"));

        UpdateUserGroupDto expectedRequest = new UpdateUserGroupDto();
        expectedRequest.setName(userGroup.getName());
        expectedRequest.setDescription(userGroup.getDescription());
        expectedRequest.setMembers(userGroup.getMembers().stream().map(UUID::fromString).collect(Collectors.toSet()));

        // Mock the behavior of groupMapper to return the expected DTO
        when(groupMapper.toUpdateUserGroupDto(any(UpdateUserGroup.class))).thenReturn(expectedRequest);

        // Act
        groupConnector.updateUserGroup(groupId, userGroup);

        // Assert
        verify(restClientMock, times(1))
                ._updateUserGroupUsingPUT(eq(groupId), updateRequestCaptor.capture());
        UpdateUserGroupDto actualRequest = updateRequestCaptor.getValue();

        assertNotNull(actualRequest);
        assertEquals(expectedRequest.getName(), actualRequest.getName());
        assertEquals(expectedRequest.getDescription(), actualRequest.getDescription());
        assertEquals(expectedRequest.getMembers(), actualRequest.getMembers());

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
                ._deleteGroupUsingDELETE(groupId);
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
                ._activateGroupUsingPOST(groupId);
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
                ._suspendGroupUsingPOST(groupId);
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
        UserGroupResource expectedResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource resourceResponse = new ClassPathResource("stubs/userGroupInfoGetUserGroupByIdSingle.json");
        byte[] resourceStreamResponse = Files.readAllBytes(resourceResponse.getFile().toPath());
        UserGroupInfo expectedGroupInfoResponse = objectMapper.readValue(resourceStreamResponse, new TypeReference<>() {
        });

        when(restClientMock._getUserGroupUsingGET(anyString())).thenReturn(ResponseEntity.of(Optional.of(expectedResponse)));

        UserGroupInfo groupInfo = groupConnector.getUserGroupById(id);
        assertEquals(expectedGroupInfoResponse, groupInfo);

        verify(restClientMock, times(1))._getUserGroupUsingGET(anyString());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserGroupById_nullModifiedBy() throws IOException {

        String id = "id";

        ClassPathResource resource = new ClassPathResource("stubs/userGroupResponseNullModifiedBy.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserGroupResource expectedResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource resourceResponse = new ClassPathResource("stubs/userGroupInfoNullModifiedBy.json");
        byte[] resourceStreamResponse = Files.readAllBytes(resourceResponse.getFile().toPath());
        UserGroupInfo expectedGroupInfo = objectMapper.readValue(resourceStreamResponse, new TypeReference<>() {
        });

        when(restClientMock._getUserGroupUsingGET(anyString())).thenReturn(ResponseEntity.of(Optional.of(expectedResponse)));

        UserGroupInfo groupInfo = groupConnector.getUserGroupById(id);
        assertEquals(expectedGroupInfo, groupInfo);
        verify(restClientMock, times(1))._getUserGroupUsingGET(anyString());
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
                ._addMemberToUserGroupUsingPUT(anyString(), any());
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
                ._deleteMemberFromUserGroupUsingDELETE(anyString(), any());
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
        Optional<UUID> userId = Optional.of(UUID.fromString("895b4af3-7fa7-4442-8a0d-a2b05c6c719f"));
        UserGroupFilter filter = new UserGroupFilter();
        filter.setInstitutionId(institutionId);
        filter.setProductId(productId);
        filter.setUserId(userId);

        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));

        ClassPathResource resource = new ClassPathResource("stubs/userGroupInfoGetUserGroups.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserGroupInfo userGroupInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource responseResource = new ClassPathResource("stubs/userGroupResponse.json");
        byte[] responseStream = Files.readAllBytes(responseResource.getFile().toPath());
        UserGroupResource response = objectMapper.readValue(responseStream, new TypeReference<>() {
        });

        // Extract page number and page size from pageable
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        // Convert Sort to a list of strings if sorting is needed and supported
        List<String> sortCriteria = pageable.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection())
                .toList();

        PageOfUserGroupResource pageOfUserGroupResource = new PageOfUserGroupResource();
        pageOfUserGroupResource.setContent(List.of(response));

        when(restClientMock._getUserGroupsUsingGET(
                filter.getInstitutionId().orElse(null),
                pageNumber,
                pageSize,
                sortCriteria,
                filter.getProductId().orElse(null),
                filter.getUserId().orElse(null),
                String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name())))
                .thenReturn(ResponseEntity.ok(pageOfUserGroupResource));

        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);

        assertEquals(1, groupInfos.getTotalElements());
        assertEquals(userGroupInfo, groupInfos.getContent().get(0));
        Mockito.verify(restClientMock, times(1))
                ._getUserGroupsUsingGET(
                        filter.getInstitutionId().orElse(null),
                        pageNumber,
                        pageSize,
                        sortCriteria,
                        filter.getProductId().orElse(null),
                        filter.getUserId().orElse(null),
                        String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name()));

        UserGroupInfo actualUserGroupInfo = groupInfos.getContent().get(0);
        assertEquals(userGroupInfo, actualUserGroupInfo);
    }

    @Test
    void getUserGroups_nullResponse_emptyInstitutionId_emptyProductId_emptyUserId_unPaged() {
        //given
        UserGroupFilter filter = new UserGroupFilter();
        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));
        when(restClientMock._getUserGroupsUsingGET(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new PageOfUserGroupResource()));
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

        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));

        UUID member = UUID.fromString("895b4af3-7fa7-4442-8a0d-a2b05c6c719f");
        UUID member2 = UUID.fromString("895b4af3-7fa7-4442-8a0d-a2b05c6c719a");
        UserGroupResource resource = mockInstance(new UserGroupResource());
        resource.setMembers(List.of(member, member2));
        resource.setCreatedAt(Instant.MAX);
        resource.setModifiedAt(Instant.MAX);

        PageOfUserGroupResource pageOfUserGroupResource = new PageOfUserGroupResource();
        pageOfUserGroupResource.setContent(List.of(resource));
        ResponseEntity<PageOfUserGroupResource> responseEntity = ResponseEntity.ok(pageOfUserGroupResource);

        when(restClientMock._getUserGroupsUsingGET(filter.getInstitutionId().orElse(null), pageable.getPageNumber(), pageable.getPageSize(), List.of("name,ASC"), filter.getProductId().orElse(null), null, String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name())))
                .thenReturn(responseEntity);
        //when
        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);

        ClassPathResource resource1 = new ClassPathResource("stubs/getUserGroups_notEmptyInstitutionId_notEmptyProductId_notEmptyUserId.json");
        byte[] resourceStream = Files.readAllBytes(resource1.getFile().toPath());
        String expectedUserGroupInfoJson = new String(resourceStream, StandardCharsets.UTF_8);

        UserGroupInfo expectedUserGroupInfo = objectMapper.readValue(expectedUserGroupInfoJson, UserGroupInfo.class);

        //then
        assertEquals(groupInfos.getContent().get(0), expectedUserGroupInfo);
        verify(restClientMock, times(1))
                ._getUserGroupsUsingGET(filter.getInstitutionId().orElse(null), pageable.getPageNumber(), pageable.getPageSize(), List.of("name,ASC"), filter.getProductId().orElse(null), null, String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name()));
    }

    @Test
    void getUserGroups_notEmptyUserId() throws IOException {

        UUID userId = UUID.randomUUID();
        UserGroupFilter filter = new UserGroupFilter();
        filter.setInstitutionId(Optional.of("institutionId"));
        filter.setUserId(Optional.of(userId));
        filter.setProductId(Optional.of("productId"));

        UUID member = UUID.fromString("895b4af3-7fa7-4442-8a0d-a2b05c6c719f");
        UUID member2 = UUID.fromString("895b4af3-7fa7-4442-8a0d-a2b05c6c719a");
        UserGroupResource resource = mockInstance(new UserGroupResource());
        resource.setMembers(List.of(member, member2));
        resource.setCreatedAt(Instant.MAX);
        resource.setModifiedAt(Instant.MAX);

        Pageable pageable = PageRequest.of(1, 1, Sort.by("name"));

        List<String> sortCriteria = pageable.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection())
                .toList();

        PageOfUserGroupResource pageOfUserGroupResource = new PageOfUserGroupResource();
        pageOfUserGroupResource.setContent(List.of(resource));
        ResponseEntity<PageOfUserGroupResource> responseEntity = ResponseEntity.ok(pageOfUserGroupResource);

        when(restClientMock._getUserGroupsUsingGET(filter.getInstitutionId().orElse(null), pageable.getPageNumber(), pageable.getPageSize(), sortCriteria, filter.getProductId().orElse(null), userId, String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name())))
                .thenReturn(responseEntity);

        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);

        ClassPathResource resource1 = new ClassPathResource("stubs/getUserGroups_notEmptyInstitutionId_notEmptyProductId_notEmptyUserId.json");
        byte[] resourceStream = Files.readAllBytes(resource1.getFile().toPath());
        String expectedUserGroupInfoJson = new String(resourceStream, StandardCharsets.UTF_8);

        UserGroupInfo expectedUserGroupInfo = objectMapper.readValue(expectedUserGroupInfoJson, UserGroupInfo.class);
        assertEquals(groupInfos.getContent().get(0), expectedUserGroupInfo);

        verify(restClientMock, times(1))
                ._getUserGroupsUsingGET(filter.getInstitutionId().orElse(null), pageable.getPageNumber(), pageable.getPageSize(), sortCriteria, filter.getProductId().orElse(null), userId, String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name()));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUserGroups_nullGroupInfos() {

        UserGroupFilter filter = new UserGroupFilter();
        filter.setInstitutionId(Optional.of("institutionId"));
        filter.setProductId(Optional.of("productId"));
        filter.setUserId(Optional.of(UUID.fromString("f36f7205-23ed-4726-b757-0ed5537fee03")));

        Pageable pageable = PageRequest.of(1, 1, Sort.by("name"));

        List<String> sortCriteria = pageable.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection())
                .toList();

        when(restClientMock._getUserGroupsUsingGET(filter.getInstitutionId().orElse(null), pageable.getPageNumber(), pageable.getPageSize(), sortCriteria, filter.getProductId().orElse(null), filter.getUserId().orElse(null), String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name())))
                .thenReturn(ResponseEntity.ok(new PageOfUserGroupResource()));

        Page<UserGroupInfo> groupInfos = groupConnector.getUserGroups(filter, pageable);

        assertNotNull(groupInfos);
        assertTrue(groupInfos.isEmpty());
        verify(restClientMock, times(1))
                ._getUserGroupsUsingGET(filter.getInstitutionId().orElse(null), pageable.getPageNumber(), pageable.getPageSize(), sortCriteria, filter.getProductId().orElse(null), filter.getUserId().orElse(null), String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name()));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void groupResponse_toGroupInfo() throws IOException {
        //given
        UserGroupResource response = mockInstance(new UserGroupResource(), "setMembers");
        UUID id = UUID.fromString("895b4af3-7fa7-4442-8a0d-a2b05c6c719f");
        UUID id1 = UUID.fromString("b339c8b4-b749-4498-82eb-1ad2e3761079");
        response.setMembers(List.of(id, id1));
        response.setCreatedAt(Instant.MAX);
        response.setModifiedAt(Instant.MAX);
        //when
        UserGroupInfo groupInfo = UserGroupConnectorImpl.GROUP_RESPONSE_TO_GROUP_INFO.apply(response);
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
        UserGroupResource response1 = mockInstance(new UserGroupResource(), "setMembers");
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
                ._deleteMemberFromUserGroupsUsingDELETE(memberId, institutionId, productId);
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
