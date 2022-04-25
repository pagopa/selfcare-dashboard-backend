package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.selfcare.dashboard.connector.model.user.Certification.NONE;
import static it.pagopa.selfcare.dashboard.connector.model.user.Certification.SPID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                UserRegistryConnectorImpl.class
        }
)
class UserRegistryConnectorImplTest {


    @Autowired
    private UserRegistryConnectorImpl userConnector;

    @MockBean
    private UserRegistryRestClient restClientMock;

    @Test
    void search_nullInfo() {
        //given
        String externalId = "externalId";
        UserResource userMock = new UserResource();
        Mockito.when(restClientMock.search(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        UserResource user = userConnector.search(externalId);
        ///then
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getEmail());
        assertNull(user.getId());
        assertNull(user.getWorkContacts());
        assertNull(user.getFiscalCode());
        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(Mockito.eq(EnumSet.allOf(UserResource.Fields.class)), embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void search_nullInfo_nullUserResponse() {
        //given
        String externalId = "externalId";
        UserResource userMock = null;
        Mockito.when(restClientMock.search(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        UserResource user = userConnector.search(externalId);
        ///then
        assertNull(user);
        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(Mockito.eq(EnumSet.allOf(UserResource.Fields.class)), embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void search_certificationNone() {
        //given
        String externalId = "externalId";
        UserResource userMock = TestUtils.mockInstance(new UserResource());
        userMock.setId(UUID.randomUUID());
        Map<String, WorkContactResource> workContacts = new HashMap<>();
        workContacts.put("institutionId", TestUtils.mockInstance(new WorkContactResource()));
        userMock.setWorkContacts(workContacts);
        Mockito.when(restClientMock.search(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        UserResource user = userConnector.search(externalId);
        ///then
        assertEquals(NONE, user.getName().getCertification());
        assertEquals(NONE, user.getEmail().getCertification());
        assertEquals(NONE, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(NONE, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(Mockito.eq(EnumSet.allOf(UserResource.Fields.class)), embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void search_nullExternalId() {
        //given
        String externalId = null;
        //when
        Executable executable = () -> userConnector.search(externalId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A TaxCode is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void search_certificationNotNone() {
        //given
        String externalId = "externalId";
        UserResource userResourceMock = TestUtils.mockInstance(new UserResource());
        userResourceMock.getEmail().setCertification(Certification.SPID);
        userResourceMock.getFamilyName().setCertification(Certification.SPID);
        userResourceMock.getName().setCertification(Certification.SPID);
        Map<String, WorkContactResource> workContacts = new HashMap<>();
        WorkContactResource workContact = TestUtils.mockInstance(new WorkContactResource());
        workContact.getEmail().setCertification(Certification.SPID);
        userResourceMock.setWorkContacts(workContacts);
        workContacts.put("institutionId", workContact);
        Mockito.when(restClientMock.search(Mockito.any(), Mockito.any()))
                .thenReturn(userResourceMock);
        //when
        UserResource user = userConnector.search(externalId);
        //then
        assertEquals(SPID, user.getName().getCertification());
        assertEquals(SPID, user.getEmail().getCertification());
        assertEquals(SPID, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(SPID, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(Mockito.eq(EnumSet.allOf(UserResource.Fields.class)), embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_nullInfo() {
        //given
        UUID userId = UUID.randomUUID();
        UserResource userMock = new UserResource();
        Mockito.when(restClientMock.getUserByInternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        UserResource user = userConnector.getUserByInternalId(userId.toString());
        ///then
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getEmail());
        assertNull(user.getId());
        assertNull(user.getWorkContacts());
        assertNull(user.getFiscalCode());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(UserResource.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_nullInfo_nullUserResponse() {
        //given
        UUID userId = UUID.randomUUID();
        UserResource userMock = null;
        Mockito.when(restClientMock.getUserByInternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        UserResource user = userConnector.getUserByInternalId(userId.toString());
        ///then
        assertNull(user);

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(UserResource.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_certificationNone() {
        //given
        UUID userId = UUID.randomUUID();

        UserResource userMock = TestUtils.mockInstance(new UserResource());
        userMock.setId(userId);
        Map<String, WorkContactResource> workContacts = new HashMap<>();
        WorkContactResource workContact = TestUtils.mockInstance(new WorkContactResource());
        workContact.getEmail().setCertification(Certification.NONE);
        userMock.setWorkContacts(workContacts);
        workContacts.put("institutionId", workContact);
        Mockito.when(restClientMock.getUserByInternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        UserResource user = userConnector.getUserByInternalId(userId.toString());
        ///then
        assertEquals(userId, user.getId());
        assertEquals(NONE, user.getName().getCertification());
        assertEquals(NONE, user.getEmail().getCertification());
        assertEquals(NONE, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(NONE, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(UserResource.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUserByInternalId_nullExternalId() {
        //given
        String userId = null;
        //when
        Executable executable = () -> userConnector.getUserByInternalId(userId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_certificationNotNone() {
        //given
        UUID userId = UUID.randomUUID();

        UserResource userMock = TestUtils.mockInstance(new UserResource());
        userMock.setId(userId);
        userMock.getEmail().setCertification(Certification.SPID);
        userMock.getFamilyName().setCertification(Certification.SPID);
        userMock.getName().setCertification(Certification.SPID);
        Map<String, WorkContactResource> workContacts = new HashMap<>();
        WorkContactResource workContact = TestUtils.mockInstance(new WorkContactResource());
        workContact.getEmail().setCertification(Certification.SPID);
        userMock.setWorkContacts(workContacts);
        workContacts.put("institutionId", workContact);
        Mockito.when(restClientMock.getUserByInternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        UserResource user = userConnector.getUserByInternalId(userId.toString());
        ///then
        assertEquals(userId, user.getId());
        assertEquals(SPID, user.getName().getCertification());
        assertEquals(SPID, user.getEmail().getCertification());
        assertEquals(SPID, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(SPID, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(UserResource.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void updateUser() {
        //given
        String institutionId = "institutionId";
        UUID id = UUID.randomUUID();
        MutableUserFieldsDto userDto = TestUtils.mockInstance(new MutableUserFieldsDto());
        //when
        Executable executable = () -> userConnector.updateUser(id, userDto);
        //then
        assertDoesNotThrow(executable);
        ArgumentCaptor<MutableUserFieldsDto> userDtoCaptor = ArgumentCaptor.forClass(MutableUserFieldsDto.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .patchUser(Mockito.any(), userDtoCaptor.capture());
        MutableUserFieldsDto request = userDtoCaptor.getValue();

        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void updateUser_nullId() {
        //given
        UUID id = null;
        MutableUserFieldsDto userDto = TestUtils.mockInstance(new MutableUserFieldsDto());

        //when
        Executable executable = () -> userConnector.updateUser(id, userDto);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A UUID is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void saveUser() {
        //given
        UserId id = TestUtils.mockInstance(new UserId());
        SaveUserDto saveUserDto = TestUtils.mockInstance(new SaveUserDto());
        Mockito.when(restClientMock.saveUser(Mockito.any()))
                .thenReturn(id);
        //when
        UserId userId = userConnector.saveUser(saveUserDto);
        //then
        assertEquals(id.getId(), userId.getId());
        ArgumentCaptor<SaveUserDto> savedDto = ArgumentCaptor.forClass(SaveUserDto.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .saveUser(savedDto.capture());
        SaveUserDto captured = savedDto.getValue();
        assertSame(saveUserDto, captured);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void saveUser_nullInfo() {
        //given
        SaveUserDto saveUserDto = TestUtils.mockInstance(new SaveUserDto());
        //when
        UserId id = userConnector.saveUser(saveUserDto);
        //then
        assertNull(id);
        ArgumentCaptor<SaveUserDto> savedDto = ArgumentCaptor.forClass(SaveUserDto.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .saveUser(savedDto.capture());
        SaveUserDto captured = savedDto.getValue();
        assertSame(saveUserDto, captured);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void deleteById() {
        //given
        UUID id = UUID.randomUUID();
        //when
        userConnector.deleteById(id.toString());
        //then
        Mockito.verify(restClientMock, Mockito.times(1))
                .deleteById(id);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void deleteById_nullId() {
        //given
        //when
        Executable executable = () -> userConnector.deleteById(null);
        //then
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A UUID is required", illegalArgumentException.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }
}