package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class UserServiceImplTest {

    @Mock
    private UserRegistryConnector userConnectorMock;

    @InjectMocks
    private UserServiceImpl userRegistryService;

    @Mock
    private MsCoreConnector msCoreConnectorMock;


    @Test
    void search() {
        //given
        String fiscalCode = "fiscalCode";
        User expectedUser = mockInstance(new User());
        when(userConnectorMock.search(any(), any()))
                .thenReturn(expectedUser);
        //when
        User user = userRegistryService.search(fiscalCode);
        //then
        assertSame(expectedUser, user);
        verify(userConnectorMock, times(1))
                .search(fiscalCode, EnumSet.of(name, familyName, email, workContacts));
        assertEquals(fiscalCode, user.getFiscalCode());
        verifyNoMoreInteractions(userConnectorMock);
    }


    @Test
    void search_nullFiscalCode() {
        //given
        String fiscalCode = null;
        //when
        Executable executable = () -> userRegistryService.search(fiscalCode);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A TaxCode is required", e.getMessage());
        verifyNoInteractions(userConnectorMock);
    }

    @Test
    void getUserByInternalId_nullId() {
        //given
        //when
        Executable executable = () -> userRegistryService.getUserByInternalId(null);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("UUID is required", e.getMessage());
        verifyNoInteractions(userConnectorMock);
    }

    @Test
    void getUserByInternalId() {
        //given
        UUID id = randomUUID();
        User userMock = mockInstance(new User());
        when(userConnectorMock.getUserByInternalId(any(), any()))
                .thenReturn(userMock);
        //when
        User user = userRegistryService.getUserByInternalId(id);
        //then
        assertSame(userMock, user);
        verify(userConnectorMock, times(1))
                .getUserByInternalId(id.toString(), EnumSet.of(name, familyName, email, fiscalCode, workContacts));
        verifyNoMoreInteractions(userConnectorMock);
    }


    @Test
    void updateUser() {
        //given
        String institutionId = "institutionId";
        UUID id = randomUUID();
        MutableUserFieldsDto user = mockInstance(new MutableUserFieldsDto(), "setWorkContacts");
        WorkContact workContact = mockInstance(new WorkContact());
        user.setWorkContacts(Map.of(institutionId, workContact));
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreConnectorMock.getInstitution(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        assertDoesNotThrow(executable);
        verify(msCoreConnectorMock, times(1))
                .getInstitution(institutionId);
        verify(userConnectorMock, times(1))
                .updateUser(id, user);
        verify(msCoreConnectorMock, times(1)).updateUser(id.toString(), institutionId);
        verifyNoMoreInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void updateUser_nullInstitution() {
        //given
        String institutionId = "institutionId";
        UUID id = randomUUID();
        MutableUserFieldsDto user = mockInstance(new MutableUserFieldsDto(), "setWorkContacts");
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("There are no institution for given institutionId", e.getMessage());
        verifyNoInteractions(userConnectorMock);
    }

    @Test
    void updateUser_nullInstitutionId() {
        //given
        String institutionId = null;
        UUID id = randomUUID();
        MutableUserFieldsDto user = mockInstance(new MutableUserFieldsDto(), "setWorkContacts");
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An institutionId is required", e.getMessage());
        verifyNoInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void updateUser_nullUUID() {
        //given
        String institutionId = "institutionId";
        UUID id = null;
        MutableUserFieldsDto user = mockInstance(new MutableUserFieldsDto(), "setWorkContacts");
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("UUID is required", e.getMessage());
        verifyNoInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void updateUser_nullDto() {
        //given
        String institutionId = "institutionId";
        UUID id = randomUUID();
        MutableUserFieldsDto user = null;
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userDto is required", e.getMessage());
        verifyNoInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void saveUser_nullInstitutionId() {
        //given
        String institutionId = null;
        SaveUserDto user = mockInstance(new SaveUserDto(), "setWorkContacts");
        //when
        Executable executable = () -> userRegistryService.saveUser(institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An institutionId is required", e.getMessage());
        verifyNoInteractions(userConnectorMock);
    }

    @Test
    void saveUser_nullDto() {
        //given
        String institutionId = "institutionId";
        SaveUserDto user = null;
        //when
        Executable executable = () -> userRegistryService.saveUser(institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userDto is required", e.getMessage());
        verifyNoInteractions(userConnectorMock);
    }

    @Test
    void saveUser_institutionNotFound() {
        //given
        String institutionId = "institutionId";
        SaveUserDto user = mockInstance(new SaveUserDto(), "setWorkContacts");
        //when
        Executable executable = () -> userRegistryService.saveUser(institutionId, user);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("There are no institution for given institutionId", e.getMessage());
        verifyNoInteractions(userConnectorMock);
    }

    @Test
    void saveUser() {
        //given
        String institutionId = "institutionId";
        UserId userId = mockInstance(new UserId());
        SaveUserDto user = mockInstance(new SaveUserDto(), "setWorkContacts");
        WorkContact workContact = mockInstance(new WorkContact());
        user.setWorkContacts(Map.of(institutionId, workContact));
        when(userConnectorMock.saveUser(any()))
                .thenReturn(userId);
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreConnectorMock.getInstitution(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        UserId id = userRegistryService.saveUser(institutionId, user);
        //then
        assertEquals(userId, id);
        verify(msCoreConnectorMock, times(1))
                .getInstitution(institutionId);
        verify(userConnectorMock, times(1))
                .saveUser(user);
        verifyNoMoreInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void deleteById_nullId() {
        //given
        //when
        Executable exe = () -> userRegistryService.deleteById(null);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, exe);
        assertEquals("A UUID is required", e.getMessage());
        verifyNoInteractions(userConnectorMock);
    }

    @Test
    void deleteById() {
        //given
        String userId = randomUUID().toString();
        //when
        userRegistryService.deleteById(userId);
        //then
        verify(userConnectorMock, times(1))
                .deleteById(userId);
    }
}