package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.pagopa.selfcare.dashboard.connector.rest.UserGroupConnectorImpl.REQUIRED_GROUP_ID_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class UserGroupConnectorImplTest {

    @Mock
    private UserGroupRestClient restClientMock;

    @InjectMocks
    private UserGroupConnectorImpl groupConnector;

    @Test
    void createGroup_nullGroup(){
        //given
        CreateUserGroup userGroup = null;
        //when
        Executable executable = () -> groupConnector.createUserGroup(userGroup);
        //then
        IllegalArgumentException e  = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User Group is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void createGroup(){
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
}