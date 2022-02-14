package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
        User expectedUser = new User();
        Mockito.when(userConnectorMock.getUser(Mockito.any()))
                .thenReturn(expectedUser);
        //when
        User user = userRegistryService.getUser(externalId);
        //then
        assertSame(expectedUser, user);
        Mockito.verify(userConnectorMock, Mockito.times(1))
                .getUser(externalId);
        Mockito.verifyNoMoreInteractions(userConnectorMock);
    }


    @Test
    void getUser_nullExternalId() {
        //given
        String externalId = null;
        //when
        Executable executable = () -> userRegistryService.getUser(externalId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A TaxCode is required", e.getMessage());
        Mockito.verifyNoInteractions(userConnectorMock);
    }

}