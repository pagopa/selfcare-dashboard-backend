package it.pagopa.selfcare.dashboard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.client.UserInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.model.mapper.GroupMapper;
import it.pagopa.selfcare.dashboard.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.model.user.UserInstitution;
import it.pagopa.selfcare.dashboard.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.exception.InvalidUserGroupException;
import it.pagopa.selfcare.dashboard.model.groups.*;
import it.pagopa.selfcare.dashboard.service.UserGroupV2ServiceImpl;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.PageOfUserGroupResource;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserDataResponse;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.model.institution.RelationshipState.SUSPENDED;
import static it.pagopa.selfcare.dashboard.service.UserGroupV2ServiceImpl.REQUIRED_GROUP_ID_MESSAGE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
/*import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserGroupV2ServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private UserGroupV2ServiceImpl userGroupV2Service;
    @Mock
    private UserGroupRestClient userGroupRestClient;
    @Mock
    private UserInstitutionApiRestClient userInstitutionApiRestClient;
    @Mock
    private UserApiRestClient userApiRestClient;
    @Spy
    private UserMapper userMapper;
    @Spy
    private GroupMapper groupMapper;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }


    @Test
    void getUserGroupById() throws IOException {
        // Arrange
        String groupId = "GroupId";
        String institutionId = "InstitutionId";
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String productId = "ProductId";

        // Load UserGroupInfo from JSON file
        ClassPathResource userGroupInfoStream = new ClassPathResource("expectations/UserGroupInfo.json");
        byte[] userGroupBytes = Files.readAllBytes(userGroupInfoStream.getFile().toPath());
        UserGroupInfo userGroupInfo = objectMapper.readValue(userGroupBytes, new TypeReference<>() {
        });

        it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource userGroupResource = new it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource();
        userGroupResource.setId("groupId");

        UserDataResponse userDataResponse = new UserDataResponse();

        // Mock the behavior of connectors
        when(userGroupRestClient._getUserGroupUsingGET(groupId)).thenReturn(ResponseEntity.ok(userGroupResource));
        when(userApiRestClient._retrieveUsers(institutionId, userId, userId, null, List.of(productId), null, List.of(ACTIVE.name(), SUSPENDED.name())))
                .thenReturn(ResponseEntity.ok(List.of(userDataResponse)));

        // Act
        UserGroupInfo result = userGroupV2Service.getUserGroupById(groupId, institutionId);

        // Assert
        assertEquals(userGroupInfo, result);
        verify(userGroupRestClient, times(1))._getUserGroupUsingGET(groupId);
        verify(userApiRestClient, times(1))._retrieveUsers(institutionId, userId, userId, null, List.of(productId), null, List.of(ACTIVE.name(), SUSPENDED.name()));
    }

    @Test
    void getUserGroupByIdMismatch() throws IOException {

        String groupId = "GroupId";
        String institutionId = "mismatchInstitutionId";

        it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource userGroupResource = new it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource();

        when(userGroupRestClient._getUserGroupUsingGET(groupId)).thenReturn(ResponseEntity.ok(userGroupResource));

        assertThrows(InvalidUserGroupException.class, () -> userGroupV2Service.getUserGroupById(groupId, institutionId));
    }

    @Test
    void getUserGroupByIdNullGroupId() {

        String institutionId = "InstitutionId";

        assertThrows(IllegalArgumentException.class, () -> userGroupV2Service.getUserGroupById(null, institutionId));
    }

    @Test
    void getUserGroupByIdNullInstitutionId() throws IOException {
        // Arrange
        String groupId = "GroupId";

        // Load UserGroupInfo from JSON file
        ClassPathResource pathResource = new ClassPathResource("expectations/UserGroupInfo.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        UserGroupInfo userGroupInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource userGroupResource = new it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource();

        // Mock the behavior of the rest client
        when(userGroupRestClient._getUserGroupUsingGET(groupId)).thenReturn(ResponseEntity.ok(userGroupResource));

        // Act
        UserGroupInfo result = userGroupV2Service.getUserGroupById(groupId, null);

        // Assert
        assertEquals(userGroupInfo, result);
        verify(userGroupRestClient, times(1))._getUserGroupUsingGET(groupId);
    }

    //    @Test
//void getUserGroupds() throws IOException {
//
//    String institutionId = "InstitutionId";
//    String productId = "ProductId";
//    UUID userId = UUID.randomUUID();
//    PageRequest pageable = PageRequest.of(0, 10);
//
//    UserGroupFilter userGroupFilter = new UserGroupFilter();
//    userGroupFilter.setInstitutionId(Optional.of(institutionId));
//    userGroupFilter.setProductId(Optional.of(productId));
//    userGroupFilter.setUserId(Optional.of(userId));
//
//    ClassPathResource pathResource = new ClassPathResource("expectations/UserGroup.json");
//    byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
//    UserGroupResource userGroup = objectMapper.readValue(resourceStream, new TypeReference<>() {
//    });
//
//  //  Page<UserGroup> mockPage = new PageImpl<>(Collections.singletonList(userGroup));
//
//    when(userGroupRestClient._getUserGroupsUsingGET(eq(institutionId), eq(0), eq(10), anyList(), eq(productId), eq(userId), anyString()))
//            .thenReturn(ResponseEntity.ok(new PageOfUserGroupResource().content(Collections.singletonList(userGroup)).totalElements(1L)));
//
//    Page<UserGroup> result = userGroupV2Service.getUserGroups(institutionId, productId, userId, pageable);
//
//    assertEquals(mockPage.getContent(), result.getContent());
//    assertEquals(mockPage.getTotalElements(), result.getTotalElements());
//    verify(userGroupRestClient, times(1))._getUserGroupsUsingGET(eq(institutionId), eq(0), eq(10), anyList(), eq(productId), eq(userId), anyString());
//}
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
        UserGroup userGroupInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
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
        pageOfUserGroupResource.setTotalElements(1L);
        pageOfUserGroupResource.setContent(List.of(response));

        when(userGroupRestClient._getUserGroupsUsingGET(
                filter.getInstitutionId().orElse(null),
                pageNumber,
                pageSize,
                sortCriteria,
                filter.getProductId().orElse(null),
                filter.getUserId().orElse(null),
                String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name())))
                .thenReturn(ResponseEntity.ok(pageOfUserGroupResource));

        when(groupMapper.toUserGroup(response)).thenReturn(userGroupInfo);

        Page<UserGroup> groupInfos = userGroupV2Service.getUserGroups(filter, pageable);

        assertEquals(1, groupInfos.getTotalElements());
        assertEquals(userGroupInfo, groupInfos.getContent().get(0));
        Mockito.verify(userGroupRestClient, times(1))
                ._getUserGroupsUsingGET(
                        filter.getInstitutionId().orElse(null),
                        pageNumber,
                        pageSize,
                        sortCriteria,
                        filter.getProductId().orElse(null),
                        filter.getUserId().orElse(null),
                        String.join(",", UserGroupStatus.ACTIVE.name(), UserGroupStatus.SUSPENDED.name()));

        UserGroup actualUserGroupInfo = groupInfos.getContent().get(0);
        assertEquals(userGroupInfo, actualUserGroupInfo);
    }

    @Test
    void getUserGroupsNullInstitutionId() throws IOException {
        // Arrange
        String productId = "ProductId";
        UUID userId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);

        UserGroupFilter userGroupFilter = new UserGroupFilter();
        userGroupFilter.setInstitutionId(Optional.empty());
        userGroupFilter.setProductId(Optional.of(productId));
        userGroupFilter.setUserId(Optional.of(userId));

        // Load UserGroup from JSON file
        ClassPathResource pathResource = new ClassPathResource("expectations/UserGroup.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        UserGroupResource userGroup = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        Page<UserGroupResource> mockPage = new PageImpl<>(Arrays.asList(userGroup, userGroup));

        // Mock the behavior of the rest client
        when(userGroupRestClient._getUserGroupsUsingGET(
                isNull(),
                eq(pageable.getPageNumber()),
                eq(pageable.getPageSize()),
                anyList(),
                eq(productId),
                eq(userId),
                anyString()
        )).thenReturn(ResponseEntity.ok(new PageOfUserGroupResource().content(Arrays.asList(userGroup, userGroup)).totalElements(2L)));

        // Act
        Page<UserGroup> result = userGroupV2Service.getUserGroups(null, productId, userId, pageable);

        // Assert
        assertEquals(mockPage, result);
        verify(userGroupRestClient, times(1))._getUserGroupsUsingGET(
                isNull(),
                eq(pageable.getPageNumber()),
                eq(pageable.getPageSize()),
                anyList(),
                eq(productId),
                eq(userId),
                anyString()
        );
    }

    @Test
    void createGroup() {
        // Arrange
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

        // Mock the behavior of the rest client
        when(userGroupRestClient._createGroupUsingPOST(any())).thenReturn(ResponseEntity.ok(new UserGroupResource().id(groupIdMock)));
        when(userInstitutionApiRestClient._retrieveUserInstitutions(anyString(), any(), anyList(), any(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(List.of(new UserInstitutionResponse().userId(id1), new UserInstitutionResponse().userId(id2), new UserInstitutionResponse().userId(id3), new UserInstitutionResponse().userId(id4))));

        // Act
        String groupId = userGroupV2Service.createUserGroup(userGroup);

        // Assert
        assertEquals(groupIdMock, groupId);
        verify(userGroupRestClient, times(1))._createGroupUsingPOST(any());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userInstitutionApiRestClient, times(1))._retrieveUserInstitutions(eq(userGroup.getInstitutionId()), any(), anyList(), any(), anyList(), anyString());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userGroupRestClient, userInstitutionApiRestClient);
    }

    @Test
    void createGroup_invalidList() {

        it.pagopa.selfcare.group.generated.openapi.v1.dto.CreateUserGroupDto userGroup = mockInstance(new it.pagopa.selfcare.group.generated.openapi.v1.dto.CreateUserGroupDto());
        CreateUserGroup createUserGroup = mockInstance(new CreateUserGroup());
        UUID id1 = randomUUID();
        UUID id2 = randomUUID();
        UUID id3 = randomUUID();
        UUID id4 = randomUUID();
        Set<UUID> userIds = Set.of(randomUUID(), id2, id3, id4);
        userGroup.setMembers(userIds);

        it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource userGroupResource = Mockito.mock( it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource.class);


        when(userGroupRestClient._createGroupUsingPOST(userGroup)).thenReturn(ResponseEntity.ok(userGroupResource));
        Executable executable = () -> userGroupV2Service.createUserGroup(createUserGroup);

        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1))
                .retrieveFilteredUserInstitution(eq(userGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userApiConnectorMock);
        Mockito.verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
    void delete() {
        // Arrange
        String groupId = "relationshipId";

        // Act
        userGroupV2Service.delete(groupId);

        // Assert
        verify(userGroupRestClient, times(1))._deleteGroupUsingDELETE(groupId);
        verifyNoMoreInteractions(userGroupRestClient);
    }

    @Test
    void delete_nullGroupId() {
        // Arrange
        Executable executable = () -> userGroupV2Service.delete(null);

        // Act & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupRestClient);
    }

    @Test
    void activate() {
        // Arrange
        String groupId = "relationshipId";

        // Act
        userGroupV2Service.activate(groupId);

        // Assert
        verify(userGroupRestClient, times(1))._activateGroupUsingPOST(groupId);
        verifyNoMoreInteractions(userGroupRestClient);
    }

    @Test
    void activate_nullGroupId() {
        // Arrange
        Executable executable = () -> userGroupV2Service.activate(null);

        // Act & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupRestClient);
    }

    @Test
    void suspend() {
        // Arrange
        String groupId = "relationshipId";

        // Act
        userGroupV2Service.suspend(groupId);

        // Assert
        verify(userGroupRestClient, times(1))._suspendGroupUsingPOST(groupId);
        verifyNoMoreInteractions(userGroupRestClient);
    }

    @Test
    void suspend_nullGroupId() {
        // Arrange
        Executable executable = () -> userGroupV2Service.suspend(null);

        // Act & Assert
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupRestClient);
    }

    @Test
    void updateUserGroup() {
        // Arrange
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
        when(userGroupRestClient._getUserGroupUsingGET(anyString()))
                .thenReturn(ResponseEntity.ok(foundGroup));

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(userInstitutionApiRestClient._retrieveUserInstitutions(anyString(), any(), anyList(), any(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(List.of(
                        new UserInstitutionResponse().userId(id1),
                        new UserInstitutionResponse().userId(id2),
                        new UserInstitutionResponse().userId(id3),
                        new UserInstitutionResponse().userId(id4)
                )));

        // Act
        Executable executable = () -> userGroupV2Service.updateUserGroup(groupId, userGroup);

        // Assert
        assertDoesNotThrow(executable);
        verify(userGroupRestClient, times(1))
                ._getUserGroupUsingGET(anyString());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userInstitutionApiRestClient, times(1))
                ._retrieveUserInstitutions(eq(foundGroup.getInstitutionId()), any(), anyList(), any(), anyList(), anyString());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(userGroupRestClient, times(1))
                ._updateUserGroupUsingPUT(anyString(), any());
        verifyNoMoreInteractions(userInstitutionApiRestClient, userGroupRestClient);
    }

    @Test
    void updateUserGroup_invalidMembersList() {
        // Arrange
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
        when(userGroupRestClient._getUserGroupUsingGET(anyString()))
                .thenReturn(ResponseEntity.ok(foundGroup));

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(userInstitutionApiRestClient._retrieveUserInstitutions(eq(foundGroup.getInstitutionId()), any(), anyList(), any(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(List.of("setId", "setId", "setId", "setId")));

        // Act
        Executable executable = () -> userGroupV2Service.updateUserGroup(groupId, userGroup);

        // Assert
        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userInstitutionApiRestClient, times(1))
                ._retrieveUserInstitutions(eq(foundGroup.getInstitutionId()), filterCaptor.capture(), anyList(), any(), anyList(), anyString());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(userGroupRestClient, times(1))
                ._getUserGroupUsingGET(anyString());
        verifyNoMoreInteractions(userInstitutionApiRestClient, userGroupRestClient);
    }


    @Test
    void testDeleteMembersByUserIdUserNotFound() {
        // Arrange
        when(userApiRestClient._retrieveUsers("institutionId", "userId", "userId", null, List.of("productId"), null, Collections.singletonList("states"))).thenReturn((ResponseEntity<List<UserDataResponse>>) Collections.emptyList());

        // Act
        userGroupV2Service.deleteMembersByUserId("userId", "institutionId", "prod-io");

        // Assert
        verify(userApiRestClient)._retrieveUsers("institutionId", "userId", "userId", null, List.of("productId"), null, Collections.singletonList("states"));
        verify(userGroupRestClient, times(1))._deleteMemberFromUserGroupsUsingDELETE(UUID.fromString("userId"), "institutionId", "prod-io");
        verifyNoMoreInteractions(userApiRestClient, userGroupRestClient);
    }

    @Test
    void testDeleteMembersByUserIdUserFound() {
        // Arrange
        when(userApiRestClient._retrieveUsers("institutionId", "userId", "userId", null, List.of("productId"), null, Collections.singletonList("states"))).thenReturn((ResponseEntity<List<UserDataResponse>>) List.of(mock(UserInstitution.class)));

        // Act
        userGroupV2Service.deleteMembersByUserId("userId", "institutionId", "prod-io");

        // Assert
        verify(userApiRestClient)._retrieveUsers("institutionId", "userId", "userId", null, List.of("productId"), null, Collections.singletonList("states"));
        verifyNoInteractions(userGroupRestClient);
    }

    @Test
    void addMemberToUserGroup() {
        // Arrange
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

        foundGroup.setMembers(List.of(userInfoMock2, userInfoMock3, userInfoMock4));
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId);
        foundGroup.setProductId(productId);

        when(userGroupRestClient._getUserGroupUsingGET(anyString()))
                .thenReturn(ResponseEntity.ok(foundGroup));

        when(userInstitutionApiRestClient._retrieveUserInstitutions(eq(institutionId), any(), anyList(), any(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(List.of(userId.toString(), "setId", "setId", "setId")));

        // Act
        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(groupId, userId);

        // Assert
        assertDoesNotThrow(executable);
        verify(userGroupRestClient, times(1))
                ._addMemberToUserGroupUsingPUT(anyString(), any());
        verify(userGroupRestClient, times(1))
                ._getUserGroupUsingGET(groupId);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userInstitutionApiRestClient, times(1))
                ._retrieveUserInstitutions(eq(institutionId), filterCaptor.capture(), anyList(), any(), anyList(), anyString());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());

        verifyNoMoreInteractions(userGroupRestClient, userInstitutionApiRestClient);
    }

    @Test
    void addMemberToUserGroup_invalidMember() {

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

        foundGroup.setMembers(List.of(userInfoMock2, userInfoMock3, userInfoMock4));
        foundGroup.setCreatedAt(Instant.now());
        foundGroup.setModifiedAt(Instant.now());
        foundGroup.setInstitutionId(institutionId);
        foundGroup.setProductId(productId);

        when(userGroupConnectorMock.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        when(userApiConnectorMock.retrieveFilteredUserInstitution(eq(institutionId), any()))
                .thenReturn(List.of("setId", "setId", "setId", "setId"));

        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(groupId, userId);

        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("This user is not allowed for this group", e.getMessage());
        verify(userGroupConnectorMock, times(1))
                .getUserGroupById(groupId);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1))
                .retrieveFilteredUserInstitution(eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userGroupConnectorMock, userApiConnectorMock);
    }

    @Test
    void addMemberToUserGroup_nullId() {
        UUID userId = randomUUID();

        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(null, userId);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupRestClient);
    }

    @Test
    void addMemberToUserGroup_nullUserId() {
        String groupId = "groupId";

        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(groupId, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(userGroupRestClient);
    }

    @Test
    void deleteMemberFromUserGroup() {
        String groupId = "groupId";
        UUID userId = randomUUID();

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(groupId, userId);

        assertDoesNotThrow(executable);
        verify(userGroupRestClient, times(1))
                ._deleteMemberFromUserGroupUsingDELETE(anyString(), any());
        verifyNoMoreInteractions(userGroupRestClient);
    }

    @Test
    void deleteMemberFromUserGroup_nullId() {
        UUID userId = randomUUID();

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(null, userId);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupRestClient);
    }

    @Test
    void deleteMemberFromUserGroup_nullUserId() {
        String groupId = "groupId";

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(groupId, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(userGroupRestClient);
    }

    @Test
    void testDeleteMembersByUserIdUserNotFound1() {
        when(userApiRestClient._retrieveUsers("institutionId", "userId", "userId", null, List.of("productId"), null, Collections.singletonList("states"))).thenReturn((ResponseEntity<List<UserDataResponse>>) Collections.emptyList());

        userGroupV2Service.deleteMembersByUserId("userId", "institutionId", "prod-io");

        verify(userApiRestClient)._retrieveUsers("institutionId", "userId", "userId", null, List.of("productId"), null, Collections.singletonList("states"));
        verify(userGroupRestClient, times(1))._deleteMemberFromUserGroupsUsingDELETE(UUID.fromString("userId"), "institutionId", "prod-io");
        verifyNoMoreInteractions(userApiRestClient, userGroupRestClient);
    }

    @Test
    void testDeleteMembersByUserIdUserFound2() {
        when(userApiRestClient._retrieveUsers("institutionId", "userId", "userId", null, List.of("productId"), null, Collections.singletonList("states"))).thenReturn((ResponseEntity<List<UserDataResponse>>) List.of(Mockito.mock(UserInstitution.class)));

        userGroupV2Service.deleteMembersByUserId("userId", "institutionId", "prod-io");

        verify(userApiRestClient)._retrieveUsers("institutionId", "userId", "userId", null, List.of("productId"), null, Collections.singletonList("states"));
        verifyNoInteractions(userGroupRestClient);
    }
}
*/
