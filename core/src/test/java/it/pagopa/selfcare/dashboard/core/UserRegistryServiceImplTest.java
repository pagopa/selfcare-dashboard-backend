package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class UserRegistryServiceImplTest {

    @Mock
    private UserRegistryConnector userConnectorMock;

    @InjectMocks
    private UserRegistryServiceImpl userRegistryService;

    @Mock
    private PartyConnector partyConnector;


    @Test
    void getUser() {
        //given
        String externalId = "externalId";
        UserResource expectedUser = TestUtils.mockInstance(new UserResource());
        Mockito.when(userConnectorMock.search(Mockito.any()))
                .thenReturn(expectedUser);
        //when
        UserResource user = userRegistryService.search(externalId);
        //then
        assertSame(expectedUser, user);
        Mockito.verify(userConnectorMock, Mockito.times(1))
                .search(externalId);
        Mockito.verifyNoMoreInteractions(userConnectorMock);
    }


    @Test
    void getUser_nullExternalId() {
        //given
        String externalId = null;
        //when
        Executable executable = () -> userRegistryService.search(externalId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A TaxCode is required", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void getUserByInternalId_nullId() {
        //given
        //when
        Executable executable = () -> userRegistryService.getUserByInternalId(null);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("UUID is required", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void getUserByInternalId() {
        //given
        UUID id = UUID.randomUUID();
        UserResource userResource = TestUtils.mockInstance(new UserResource());
        Mockito.when(userConnectorMock.getUserByInternalId(Mockito.anyString()))
                .thenReturn(userResource);
        //when
        UserResource user = userRegistryService.getUserByInternalId(id);
        //then
        assertSame(userResource, user);
        Mockito.verify(userConnectorMock, Mockito.times(1))
                .getUserByInternalId(id.toString());
        Mockito.verifyNoMoreInteractions(userConnectorMock);
    }


    @Test
    void updateUser() {
        //given
        String institutionId = "institutionId";
        UUID id = UUID.randomUUID();
        UserDto user = TestUtils.mockInstance(new UserDto());
        WorkContact workContact = TestUtils.mockInstance(new WorkContact());
        user.setWorkContacts(Map.of(institutionId, workContact));
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitution(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitution(institutionId);
        ArgumentCaptor<MutableUserFieldsDto> mutableFieldsCaptor = ArgumentCaptor.forClass(MutableUserFieldsDto.class);
        Mockito.verify(userConnectorMock, Mockito.times(1))
                .updateUser(Mockito.any(), mutableFieldsCaptor.capture());
        MutableUserFieldsDto capturedFields = mutableFieldsCaptor.getValue();
        assertTrue(capturedFields.getWorkContacts().containsKey(institutionId));
        assertEquals(user.getWorkContacts().get(institutionId).getEmail(), capturedFields.getWorkContacts().get(institutionId).getEmail().getValue());
        assertEquals(user.getFamilyName(), capturedFields.getFamilyName().getValue());
        assertEquals(user.getEmail(), capturedFields.getWorkContacts().get(institutionId).getEmail().getValue());
        Mockito.verifyNoMoreInteractions(userConnectorMock, partyConnector);
    }

    @Test
    void updateUser_nullInstitution() {
        //given
        String institutionId = "institutionId";
        UUID id = UUID.randomUUID();
        UserDto user = TestUtils.mockInstance(new UserDto());
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("There are no institution for given institutionId", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void updateUser_nullInstitutionId() {
        //given
        String institutionId = null;
        UUID id = UUID.randomUUID();
        UserDto user = TestUtils.mockInstance(new UserDto());
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An institutionId is required", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void updateUser_nullUUID() {
        //given
        String institutionId = "institutionId";
        UUID id = null;
        UserDto user = TestUtils.mockInstance(new UserDto());
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("UUID is required", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void updateUser_nullDto() {
        //given
        String institutionId = "institutionId";
        UUID id = UUID.randomUUID();
        UserDto user = null;
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userDto is required", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void saveUser_nullInstitutionId() {
        //given
        String institutionId = null;
        SaveUser user = TestUtils.mockInstance(new SaveUser());
        //when
        Executable executable = () -> userRegistryService.saveUser(institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An institutionId is required", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void saveUser_nullDto() {
        //given
        String institutionId = "institutionId";
        SaveUser user = null;
        //when
        Executable executable = () -> userRegistryService.saveUser(institutionId, user);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userDto is required", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void saveUser_institutionNotFound() {
        //given
        String institutionId = "institutionId";
        SaveUser user = TestUtils.mockInstance(new SaveUser());
        //when
        Executable executable = () -> userRegistryService.saveUser(institutionId, user);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("There are no institution for given institutionId", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

    @Test
    void saveUser() {
        //given
        String institutionId = "institutionId";
        UserId userId = TestUtils.mockInstance(new UserId());
        SaveUser user = TestUtils.mockInstance(new SaveUser());
        WorkContact workContact = TestUtils.mockInstance(new WorkContact());
        user.setWorkContacts(Map.of(institutionId, workContact));
        Mockito.when(userConnectorMock.saveUser(Mockito.any()))
                .thenReturn(userId);
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitution(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        UserId id = userRegistryService.saveUser(institutionId, user);
        //then
        assertEquals(userId, id);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitution(institutionId);
        ArgumentCaptor<SaveUserDto> saveCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        Mockito.verify(userConnectorMock, Mockito.times(1))
                .saveUser(saveCaptor.capture());
        SaveUserDto capturedSave = saveCaptor.getValue();
        assertEquals(user.getEmail(), capturedSave.getEmail().getValue());
        assertEquals(user.getFamilyName(), capturedSave.getFamilyName().getValue());
        assertEquals(user.getName(), capturedSave.getName().getValue());
        assertTrue(capturedSave.getWorkContacts().containsKey(institutionId));
        assertEquals(user.getFiscalCode(), capturedSave.getFiscalCode());
        Mockito.verifyNoMoreInteractions(userConnectorMock, partyConnector);
    }

}