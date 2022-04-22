package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.UserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class UserRegistryServiceImplTest {

    @Mock
    private UserRegistryConnector userConnectorMock;

    @InjectMocks
    private UserRegistryServiceImpl userRegistryService;


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
    void updateUser() {
        //given
        String institutionId = "institutionId";
        UUID id = UUID.randomUUID();
        UserDto user = TestUtils.mockInstance(new UserDto());
        //when
        Executable executable = () -> userRegistryService.updateUser(id, institutionId, user);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(userConnectorMock, Mockito.times(1))
                .updateUser(Mockito.any(), Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(userConnectorMock);
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

}