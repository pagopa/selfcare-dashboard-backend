package it.pagopa.selfcare.dashboard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.client.UserInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.exception.InvalidUserGroupException;
import it.pagopa.selfcare.dashboard.model.groups.*;
import it.pagopa.selfcare.dashboard.model.mapper.GroupMapper;
import it.pagopa.selfcare.dashboard.model.mapper.GroupMapperImpl;
import it.pagopa.selfcare.dashboard.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.model.mapper.UserMapperImpl;
import it.pagopa.selfcare.dashboard.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.model.user.UserInstitution;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.PageOfUserGroupResource;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGroupV2ServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private UserGroupV2ServiceImpl userGroupV2Service;

    @Mock
    private UserGroupRestClient userGroupRestClient;

    @Mock
    private UserInstitutionApiRestClient userInstitutionApiRestClient;

    @Mock
    private UserApiRestClient userApiRestClient;

    @Spy
    private GroupMapperImpl groupMapper;

    @Spy
    private UserMapperImpl userMapper;

    @BeforeEach
    void init() {
        super.setUp();
    }


    @Test
    void getUserGroupByIdMismatch() {
        String groupId = "GroupId";
        String institutionId = "mismatchInstitutionId";
        when(userGroupRestClient._getUserGroupUsingGET(groupId)).thenReturn(ResponseEntity.ok(mockInstance(new UserGroupResource())));
        assertThrows(InvalidUserGroupException.class, () -> userGroupV2Service.getUserGroupById(groupId, institutionId));
    }

    @Test
    void getUserGroupByIdNullGroupId() {
        String institutionId = "InstitutionId";
        assertThrows(IllegalArgumentException.class, () -> userGroupV2Service.getUserGroupById(null, institutionId));
    }

    @Test
    void getUserGroups() throws IOException {

        String institutionId = "setInstitutionId";
        String productId = "ProductId";
        UUID userId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);

        UserGroupFilter userGroupFilter = new UserGroupFilter();
        userGroupFilter.setInstitutionId(Optional.of(institutionId));
        userGroupFilter.setProductId(Optional.of(productId));
        userGroupFilter.setUserId(Optional.of(userId));

        ClassPathResource pathResource = new ClassPathResource("expectations/UserGroup.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        UserGroup userGroup = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        Page<UserGroup> mockPage = new PageImpl<>(List.of(userGroup, userGroup));
        PageOfUserGroupResource pageOfUserGroupResource = new PageOfUserGroupResource();
        pageOfUserGroupResource.setTotalElements(2L);
        pageOfUserGroupResource.setContent(Arrays.asList(mockInstance(new UserGroupResource()), mockInstance(new UserGroupResource())));
        when(userGroupRestClient._getUserGroupsUsingGET(any(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(ResponseEntity.ok(pageOfUserGroupResource));

        Page<UserGroup> result = userGroupV2Service.getUserGroups(institutionId, productId, userId, pageable);

        assertEquals(mockPage.getTotalElements(), result.getTotalElements());
        assertEquals(mockPage.getContent().get(0).getInstitutionId(), result.getContent().get(0).getInstitutionId());
    }

    @Test
    void getUserGroupsNullInstitutionId() {

        String productId = "ProductId";
        UUID userId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);

        UserGroupFilter userGroupFilter = new UserGroupFilter();
        userGroupFilter.setInstitutionId(Optional.empty());
        userGroupFilter.setProductId(Optional.of(productId));
        userGroupFilter.setUserId(Optional.of(userId));

        UserGroup mockGroup1 = mockInstance(new UserGroup());
        UserGroup mockGroup2 = mockInstance(new UserGroup());
        Page<UserGroup> mockPage = new PageImpl<>(Arrays.asList(mockGroup1, mockGroup2));
        PageOfUserGroupResource pageOfUserGroupResource = new PageOfUserGroupResource();
        pageOfUserGroupResource.setTotalElements(2L);
        pageOfUserGroupResource.setContent(Arrays.asList(mockInstance(new UserGroupResource()), mockInstance(new UserGroupResource())));
        when(userGroupRestClient._getUserGroupsUsingGET(any(), any(), any(), any(), any(), any(), anyString())).thenReturn(ResponseEntity.ok(pageOfUserGroupResource));
        Page<UserGroup> result = userGroupV2Service.getUserGroups(null, productId, userId, pageable);

        assertEquals(mockPage.getTotalElements(), result.getTotalElements());
        assertEquals(mockPage.getContent().get(0).getInstitutionId(), result.getContent().get(0).getInstitutionId());
    }

    @Test
    void createGroup() {

        CreateUserGroup userGroup = mockInstance(new CreateUserGroup());
        String id1 = randomUUID().toString();
        List<String> userIds = List.of(id1);

        userGroup.setMembers(userIds);
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        userInfoMock1.setId(id1);

        UserInstitutionResponse userInstitutionResponse = mockInstance(new UserInstitutionResponse(), "setUserId");
        userInstitutionResponse.setUserId(id1);

        when(userInstitutionApiRestClient._retrieveUserInstitutions("setInstitutionId", null, List.of("setProductId"), null, List.of("ACTIVE","SUSPENDED"),null))
                .thenReturn(ResponseEntity.ok(Collections.singletonList(userInstitutionResponse)));
        when(userGroupRestClient._createGroupUsingPOST(any())).thenReturn(ResponseEntity.ok(mockInstance(new UserGroupResource())));
        String groupId = userGroupV2Service.createUserGroup(userGroup);

        assertEquals("setId", groupId);
    }

    @Test
    void createGroup_invalidList() {

        CreateUserGroup userGroup = mockInstance(new CreateUserGroup());
        String id1 = randomUUID().toString();
        List<String> userIds = List.of(id1);

        userGroup.setMembers(userIds);
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        userInfoMock1.setId(id1);
        when(userInstitutionApiRestClient._retrieveUserInstitutions("setInstitutionId", null, List.of("setProductId"), null, List.of("ACTIVE","SUSPENDED"),null))
                .thenReturn(ResponseEntity.ok(Collections.singletonList(mockInstance(new UserInstitutionResponse()))));

        Executable executable = () -> userGroupV2Service.createUserGroup(userGroup);

        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());
    }

    @Test
    void delete() {

        String groupId = "relationshipId";

        userGroupV2Service.delete(groupId);
    }

    @Test
    void delete_nullGroupId() {

        Executable executable = () -> userGroupV2Service.delete(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
    }

    @Test
    void activate() {

        String groupId = "relationshipId";

        userGroupV2Service.activate(groupId);
    }

    @Test
    void activate_nullGroupId() {

        Executable executable = () -> userGroupV2Service.activate(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
    }

    @Test
    void suspend() {

        String groupId = "relationshipId";

        userGroupV2Service.suspend(groupId);
    }

    @Test
    void suspend_nullGroupId() {

        Executable executable = () -> userGroupV2Service.suspend(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
    }



    @Test
    void testDeleteMembersByUserIdUserNotFound() {
        when(userInstitutionApiRestClient._retrieveUserInstitutions(any(), any(), any(), any(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(Collections.singletonList(mockInstance(new UserInstitutionResponse(), "setInstitutionId"))));
        userGroupV2Service.deleteMembersByUserId(UUID.randomUUID().toString(), "institutionId", "prod-io");
        verifyNoInteractions(userGroupRestClient);
    }


    @Test
    void testDeleteMembersByUserIdUserFound() {
        when(userInstitutionApiRestClient._retrieveUserInstitutions(any(), any(), any(), any(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));
        when(userGroupRestClient._deleteMemberFromUserGroupsUsingDELETE(any(), any(), any()))
                .thenReturn(ResponseEntity.ok().build());
        Assertions.assertDoesNotThrow(() -> userGroupV2Service.deleteMembersByUserId(UUID.randomUUID().toString(), "institutionId", "prod-io"));
    }

/*    @Test
    void addMemberToUserGroup() {

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

        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(groupId, userId);

        assertDoesNotThrow(executable);
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
    }*/


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

        UserGroupResource userGroupResource = new UserGroupResource();
        userGroupResource.setProductId(productId);
        when(userGroupRestClient._getUserGroupUsingGET(anyString())).thenReturn(ResponseEntity.ok(userGroupResource));
        when(userInstitutionApiRestClient._retrieveUserInstitutions(null, null, List.of("productId"),null, List.of("ACTIVE","SUSPENDED"),null))
                .thenReturn(ResponseEntity.ok(Collections.singletonList(mockInstance(new UserInstitutionResponse(), "setInstitutionId"))));

        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(groupId, userId);

        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("This user is not allowed for this group", e.getMessage());
    }

    @Test
    void addMemberToUserGroup_nullId() {

        UUID userId = randomUUID();

        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(null, userId);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
    }

    @Test
    void addMemberToUserGroup_nullUserId() {

        String groupId = "groupId";

        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(groupId, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
    }

    @Test
    void deleteMemberFromUserGroup() {

        String groupId = "groupId";
        UUID userId = randomUUID();

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(groupId, userId);

        assertDoesNotThrow(executable);
    }

    @Test
    void deleteMemberFromUserGroup_nullId() {

        UUID userId = randomUUID();

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(null, userId);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
    }

    @Test
    void deleteMemberFromUserGroup_nullUserId() {

        String groupId = "groupId";

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(groupId, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
    }

    @Test
    void testDeleteMembersByUserIdUserNotFound1() {
        when(userInstitutionApiRestClient._retrieveUserInstitutions(any(), any(), any(), any(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));
        userGroupV2Service.deleteMembersByUserId(UUID.randomUUID().toString(), "institutionId", "prod-io");
        Assertions.assertDoesNotThrow(() -> userGroupV2Service.deleteMembersByUserId(UUID.randomUUID().toString(), "institutionId", "prod-io"));
    }


    @Test
    void testDeleteMembersByUserIdUserFound2() {
        when(userInstitutionApiRestClient._retrieveUserInstitutions(any(), any(), any(), any(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(Collections.singletonList(mockInstance(new UserInstitutionResponse(), "setInstitutionId"))));
        Assertions.assertDoesNotThrow(() -> userGroupV2Service.deleteMembersByUserId(UUID.randomUUID().toString(), "institutionId", "prod-io"));
        verifyNoInteractions(userGroupRestClient);
    }
}
