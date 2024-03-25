package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class UserV2ServiceImplTest {

    @Mock
    private UserRegistryConnector userConnectorMock;

    @InjectMocks
    private UserV2ServiceImpl userService;

    @Mock
    private MsCoreConnector msCoreConnectorMock;

    @Mock
    private UserApiConnector userApiConnector;

    @Mock
    private UserGroupV2Service userGroupService;

    @Test
    void getInstitutions() {
        // given
        String userId = "userId";
        InstitutionBase expectedInstitutionInfo = new InstitutionBase();

        when(userApiConnector.getUserInstitutions(userId)).thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionBase> institutions = userService.getInstitutions(userId);

        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verifyNoMoreInteractions(msCoreConnectorMock);
    }


    @Test
    void suspend() {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        // when
        userService.suspendUserProduct(userId, institutionid, productId);
        Mockito.verify(userApiConnector, Mockito.times(1))
                .suspendUserProduct(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userApiConnector);
    }


    @Test
    void activate() {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        // when
        userService.activateUserProduct(userId, institutionid, productId);
        // then
        Mockito.verify(userApiConnector, Mockito.times(1))
                .activateUserProduct(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userApiConnector);
    }

    @Test
    void delete() {
        //given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        //when
        userService.deleteUserProduct(userId, institutionid, productId);
        //then
        Mockito.verify(userApiConnector, Mockito.times(1))
                .deleteUserProduct(userId, institutionid, productId);
        Mockito.verify(userGroupService, Mockito.times(1))
                .deleteMembersByUserId(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userApiConnector, userGroupService);
    }

    @Test
    void getById(){
        //given
        String userId = "userId";
        User user = mockInstance(new User());
        when(userApiConnector.getUserById(userId, null)).thenReturn(user);
        //when
        User result = userService.getUserById(userId);
        //then
        assertNotNull(result);
        assertEquals(user, result);
        verify(userApiConnector, times(1)).getUserById(userId, null);
    }

    @Test
    void searchByFiscalCode(){
        String fiscalCode = "fiscalCode";
        User user = mockInstance(new User());
        when(userApiConnector.searchByFiscalCode(anyString())).thenReturn(user);
        //when
        User result = userService.searchUserByFiscalCode(fiscalCode);
        //then
        assertNotNull(result);
        assertEquals(user, result);
        verify(userApiConnector, times(1)).searchByFiscalCode(fiscalCode);
    }

    @Test
    void updateUser() {
        //given
        final String institutionId = "institutionId";
        final String userId = "userId";
        final MutableUserFieldsDto user = mockInstance(new MutableUserFieldsDto(), "setWorkContacts");
        WorkContact workContact = mockInstance(new WorkContact());
        user.setWorkContacts(Map.of(institutionId, workContact));
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreConnectorMock.getInstitution(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> userService.updateUser(userId, institutionId, user);
        //then
        assertDoesNotThrow(executable);
        verify(msCoreConnectorMock, times(1))
                .getInstitution(institutionId);
        verify(userApiConnector, times(1))
                .updateUser(userId, institutionId, user);
        verifyNoMoreInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void updateUser_nullInstitution() {
        //given
        final String institutionId = "institutionId";
        final String userId = "userId";
        final MutableUserFieldsDto user = mockInstance(new MutableUserFieldsDto(), "setWorkContacts");
        //when
        Executable executable = () -> userService.updateUser(userId, institutionId, user);
        //then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("There is no institution for given institutionId", exception.getMessage());
        verifyNoInteractions(userApiConnector);
    }

    @Test
    void getUsersByInstitutionId_returnsExpectedUsers() {
        // given
        String institutionId = "inst1";
        String productId = "prod1";
        String loggedUserId = "loggedUserId";
        UserInfo expectedUser = new UserInfo();
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);

        when(userApiConnector.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(List.of(expectedUser));
        // when
        Collection<UserInfo> users = userService.getUsersByInstitutionId(institutionId, productId, loggedUserId);

        // then
        assertNotNull(users);
        assertEquals(1, users.size());
        assertSame(expectedUser, users.iterator().next());
        verifyNoMoreInteractions(userApiConnector);
    }

    @Test
    void getUsersByInstitutionId_emptyList() {
        // given
        String institutionId = "inst1";
        String productId = "prod1";
        String loggedUserId = "loggedUserId";

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);

        when(userApiConnector.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(Collections.emptyList());
        // when
        Collection<UserInfo> users = userService.getUsersByInstitutionId(institutionId, productId, loggedUserId);

        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verifyNoMoreInteractions(userApiConnector);
    }
}