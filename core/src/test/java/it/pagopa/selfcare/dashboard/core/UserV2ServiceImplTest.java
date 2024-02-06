package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

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
        doNothing().when(userApiConnector).updateUser(id.toString(), institutionId, user);
        //when
        Executable executable = () -> userService.updateUser(id, institutionId, user);
        //then
        assertDoesNotThrow(executable);
        verify(msCoreConnectorMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void updateUser_nullInstitution() {
        //given
        String institutionId = "institutionId";
        UUID id = randomUUID();
        MutableUserFieldsDto user = mockInstance(new MutableUserFieldsDto(), "setWorkContacts");
        //when
        Executable executable = () -> userService.updateUser(id, institutionId, user);
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
        Executable executable = () -> userService.updateUser(id, institutionId, user);
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
        Executable executable = () -> userService.updateUser(id, institutionId, user);
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
        //when
        Executable executable = () -> userService.updateUser(id, institutionId, null);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userDto is required", e.getMessage());
        verifyNoInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void getInstitutions() {
        // given
        String userId = "userId";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();

        when(userApiConnector.getUserProducts(userId)).thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = userService.getInstitutions(userId);

        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

}
