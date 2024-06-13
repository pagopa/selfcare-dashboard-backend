package it.pagopa.selfcare.dashboard.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupFilter;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitution;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.SUSPENDED;
import static it.pagopa.selfcare.dashboard.core.UserGroupV2ServiceImpl.REQUIRED_GROUP_ID_MESSAGE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserGroupV2ServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private UserGroupV2ServiceImpl userGroupV2Service;
    @Mock
    private UserApiConnector userApiConnectorMock;
    @Mock
    private UserGroupConnector userGroupConnectorMock;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }


    @Test
    void getUserGroupById() throws IOException {

        String groupId = "GroupId";
        String institutionId = "InstitutionId";

        ClassPathResource pathResource = new ClassPathResource("expectations/UserGroupInfo.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        UserGroupInfo userGroupInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiConnectorMock.retrieveFilteredUserInstitution(any(), any()))
                .thenReturn(List.of("setId", "setId", "setId", "setId"));
        when(userGroupConnectorMock.getUserGroupById(groupId)).thenReturn(userGroupInfo);

        UserGroupInfo result = userGroupV2Service.getUserGroupById(groupId, institutionId);

        assertEquals(userGroupInfo, result);
        verify(userGroupConnectorMock, times(1)).getUserGroupById(groupId);
    }

    @Test
    void getUserGroupByIdMismatch() throws IOException {

        String groupId = "GroupId";
        String institutionId = "mismatchInstitutionId";

        ClassPathResource pathResource = new ClassPathResource("expectations/UserGroupInfo.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        UserGroupInfo userGroupInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userGroupConnectorMock.getUserGroupById(groupId)).thenReturn(userGroupInfo);

        assertThrows(InvalidUserGroupException.class, () -> userGroupV2Service.getUserGroupById(groupId, institutionId));
    }

    @Test
    void getUserGroupByIdNullGroupId() {

        String institutionId = "InstitutionId";

        assertThrows(IllegalArgumentException.class, () -> userGroupV2Service.getUserGroupById(null, institutionId));
    }

    @Test
    void getUserGroupByIdNullInstitutionId() throws IOException {

        String groupId = "GroupId";

        ClassPathResource pathResource = new ClassPathResource("expectations/UserGroupInfo.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        UserGroupInfo userGroupInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userGroupConnectorMock.getUserGroupById(groupId)).thenReturn(userGroupInfo);

        UserGroupInfo result = userGroupV2Service.getUserGroupById(groupId, null);

        assertEquals(userGroupInfo, result);
        verify(userGroupConnectorMock, times(1)).getUserGroupById(groupId);
    }

    @Test
    void getUserGroups() throws IOException {

        String institutionId = "InstitutionId";
        String productId = "ProductId";
        UUID userId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);

        UserGroupFilter userGroupFilter = new UserGroupFilter();
        userGroupFilter.setInstitutionId(Optional.of(institutionId));
        userGroupFilter.setProductId(Optional.of(productId));
        userGroupFilter.setUserId(Optional.of(userId));

        ClassPathResource pathResource = new ClassPathResource("expectations/UserGroupInfo.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        UserGroupInfo userGroupInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        Page<UserGroupInfo> mockPage = new PageImpl<>(Collections.singletonList(userGroupInfo));

        when(userGroupConnectorMock.getUserGroups(userGroupFilter, pageable)).thenReturn(mockPage);

        Page<UserGroupInfo> result = userGroupV2Service.getUserGroups(institutionId, productId, userId, pageable);

        assertEquals(mockPage, result);
        verify(userGroupConnectorMock, times(1)).getUserGroups(any(), eq(pageable));
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

        UserGroupInfo mockGroupInfo1 = new UserGroupInfo();
        UserGroupInfo mockGroupInfo2 = new UserGroupInfo();
        Page<UserGroupInfo> mockPage = new PageImpl<>(Arrays.asList(mockGroupInfo1, mockGroupInfo2));

        when(userGroupConnectorMock.getUserGroups(any(), eq(pageable))).thenReturn(mockPage);

        Page<UserGroupInfo> result = userGroupV2Service.getUserGroups(null, productId, userId, pageable);

        assertEquals(mockPage, result);
        verify(userGroupConnectorMock, times(1)).getUserGroups(any(), eq(pageable));
    }

    @Test
    void createGroup() {

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
        when(userGroupConnectorMock.createUserGroup(any()))
                .thenReturn(groupIdMock);

        when(userApiConnectorMock.retrieveFilteredUserInstitution(anyString(), any()))
                .thenReturn(List.of(id1, id2, id3, id4));   //when
        String groupId = userGroupV2Service.createUserGroup(userGroup);

        assertEquals(groupIdMock, groupId);
        verify(userGroupConnectorMock, times(1))
                .createUserGroup(any());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1))
                .retrieveFilteredUserInstitution(eq(userGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(userGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verifyNoMoreInteractions(userGroupConnectorMock, userApiConnectorMock);
    }

    @Test
    void createGroup_invalidList() {

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

        when(userApiConnectorMock.retrieveFilteredUserInstitution(eq(userGroup.getInstitutionId()), any()))
                .thenReturn(List.of("setId", "setId", "setId", "setId"));   //when
        Executable executable = () -> userGroupV2Service.createUserGroup(userGroup);

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

        String groupId = "relationshipId";

        userGroupV2Service.delete(groupId);
        verify(userGroupConnectorMock, times(1))
                .delete(groupId);
        verifyNoMoreInteractions(userGroupConnectorMock);
    }

    @Test
    void delete_nullGroupId() {

        Executable executable = () -> userGroupV2Service.delete(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
    void activate() {

        String groupId = "relationshipId";

        userGroupV2Service.activate(groupId);
        verify(userGroupConnectorMock, times(1))
                .activate(groupId);
        verifyNoMoreInteractions(userGroupConnectorMock);
    }

    @Test
    void activate_nullGroupId() {

        Executable executable = () -> userGroupV2Service.activate(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
    void suspend() {

        String groupId = "relationshipId";

        userGroupV2Service.suspend(groupId);
        verify(userGroupConnectorMock, times(1))
                .suspend(groupId);
        verifyNoMoreInteractions(userGroupConnectorMock);
    }

    @Test
    void suspend_nullGroupId() {

        Executable executable = () -> userGroupV2Service.suspend(null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
    void updateUserGroup() {

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
        when(userGroupConnectorMock.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(userApiConnectorMock.retrieveFilteredUserInstitution(anyString(), any()))
                .thenReturn(List.of(id1, id2, id3, id4)); //when
        Executable executable = () -> userGroupV2Service.updateUserGroup(groupId, userGroup);

        assertDoesNotThrow(executable);
        verify(userGroupConnectorMock, times(1))
                .getUserGroupById(anyString());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1))
                .retrieveFilteredUserInstitution(eq(foundGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(userGroupConnectorMock, times(1))
                .updateUserGroup(anyString(), any());
        verifyNoMoreInteractions(userApiConnectorMock, userGroupConnectorMock);
    }

    @Test
    void updateUserGroup_invalidMembersList() {

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
        when(userGroupConnectorMock.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        when(userApiConnectorMock.retrieveFilteredUserInstitution(eq(foundGroup.getInstitutionId()), any()))
                .thenReturn(List.of("setId", "setId", "setId", "setId"));   //when
        Executable executable = () -> userGroupV2Service.updateUserGroup(groupId, userGroup);

        InvalidMemberListException e = assertThrows(InvalidMemberListException.class, executable);
        assertEquals("Some members in the list aren't allowed for this institution", e.getMessage());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(userApiConnectorMock, times(1))
                .retrieveFilteredUserInstitution(eq(foundGroup.getInstitutionId()), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getUserId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getRole());
        assertEquals(foundGroup.getProductId(), capturedFilter.getProductId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(userGroupConnectorMock, times(1))
                .getUserGroupById(anyString());
        verifyNoMoreInteractions(userApiConnectorMock, userGroupConnectorMock);
    }


    @Test
    void testDeleteMembersByUserIdUserNotFound() {

        when(userApiConnectorMock.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(Collections.emptyList());

        userGroupV2Service.deleteMembersByUserId("userId", "institutionId", "prod-io");
        verify(userApiConnectorMock).retrieveFilteredUser("userId", "institutionId", "prod-io");
    }


    @Test
    void testDeleteMembersByUserIdUserFound() {

        when(userApiConnectorMock.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(List.of(Mockito.mock(UserInstitution.class)));

        userGroupV2Service.deleteMembersByUserId("userId", "institutionId", "prod-io");
        verify(userApiConnectorMock).retrieveFilteredUser("userId", "institutionId", "prod-io");
        verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
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

        when(userGroupConnectorMock.getUserGroupById(anyString()))
                .thenReturn(foundGroup);

        when(userApiConnectorMock.retrieveFilteredUserInstitution(eq(institutionId), any()))
                .thenReturn(List.of(userId.toString(), "setId", "setId", "setId"));  //when
        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(groupId, userId);

        assertDoesNotThrow(executable);
        verify(userGroupConnectorMock, times(1))
                .addMemberToUserGroup(anyString(), any());
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
        Mockito.verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
    void addMemberToUserGroup_nullUserId() {

        String groupId = "groupId";

        Executable executable = () -> userGroupV2Service.addMemberToUserGroup(groupId, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
    void deleteMemberFromUserGroup() {

        String groupId = "groupId";
        UUID userId = randomUUID();

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(groupId, userId);

        assertDoesNotThrow(executable);
        verify(userGroupConnectorMock, times(1))
                .deleteMemberFromUserGroup(anyString(), any());
        verifyNoMoreInteractions(userGroupConnectorMock);
    }

    @Test
    void deleteMemberFromUserGroup_nullId() {

        UUID userId = randomUUID();

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(null, userId);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GROUP_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
    void deleteMemberFromUserGroup_nullUserId() {

        String groupId = "groupId";

        Executable executable = () -> userGroupV2Service.deleteMemberFromUserGroup(groupId, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(userGroupConnectorMock);
    }

    @Test
    void testDeleteMembersByUserIdUserNotFound1() {

        when(userApiConnectorMock.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(Collections.emptyList());

        userGroupV2Service.deleteMembersByUserId("userId", "institutionId", "prod-io");
        verify(userApiConnectorMock).retrieveFilteredUser("userId", "institutionId", "prod-io");
    }


    @Test
    void testDeleteMembersByUserIdUserFound2() {

        when(userApiConnectorMock.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(List.of(Mockito.mock(UserInstitution.class)));

        userGroupV2Service.deleteMembersByUserId("userId", "institutionId", "prod-io");
        verify(userApiConnectorMock).retrieveFilteredUser("userId", "institutionId", "prod-io");
        verifyNoInteractions(userGroupConnectorMock);
    }
}
