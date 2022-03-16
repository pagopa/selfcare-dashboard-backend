package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InvalidMemberListException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    @Test
    void createGroup(){
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
    void createGroup_invalidList(){
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
}