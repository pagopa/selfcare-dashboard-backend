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
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = new User();
        Mockito.when(restClientMock.search(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId, fieldList);
        ///then
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getEmail());
        assertNull(user.getId());
        assertNull(user.getWorkContacts());
        assertNull(user.getFiscalCode());
        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(embeddedCaptor.capture(), Mockito.eq(EnumSet.allOf(User.Fields.class)));
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void search_nullInfo_nullUserResponse() {
        //given
        String externalId = "externalId";
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = null;
        Mockito.when(restClientMock.search(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId, fieldList);
        ///then
        assertNull(user);
        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(embeddedCaptor.capture(), Mockito.eq(EnumSet.allOf(User.Fields.class)));
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void search_certificationNone() {
        //given
        String externalId = "externalId";
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = TestUtils.mockInstance(new User());
        userMock.setId(UUID.randomUUID().toString());
        Map<String, WorkContactResource> workContacts = new HashMap<>();
        workContacts.put("institutionId", TestUtils.mockInstance(new WorkContactResource()));
        userMock.setWorkContacts(workContacts);
        Mockito.when(restClientMock.search(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId, fieldList);
        ///then
        assertEquals(NONE, user.getName().getCertification());
        assertEquals(NONE, user.getEmail().getCertification());
        assertEquals(NONE, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(NONE, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(embeddedCaptor.capture(), Mockito.eq(EnumSet.allOf(User.Fields.class)));
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void search_nullExternalId() {
        //given
        String externalId = null;
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        //when
        Executable executable = () -> userConnector.search(externalId, fieldList);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A TaxCode is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void search_certificationNotNone() {
        //given
        String externalId = "externalId";
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = TestUtils.mockInstance(new User());
        userMock.getEmail().setCertification(Certification.SPID);
        userMock.getFamilyName().setCertification(Certification.SPID);
        userMock.getName().setCertification(Certification.SPID);
        Map<String, WorkContactResource> workContacts = new HashMap<>();
        WorkContactResource workContact = TestUtils.mockInstance(new WorkContactResource());
        workContact.getEmail().setCertification(Certification.SPID);
        userMock.setWorkContacts(workContacts);
        workContacts.put("institutionId", workContact);
        Mockito.when(restClientMock.search(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId, fieldList);
        //then
        assertEquals(SPID, user.getName().getCertification());
        assertEquals(SPID, user.getEmail().getCertification());
        assertEquals(SPID, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(SPID, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(embeddedCaptor.capture(), Mockito.eq(EnumSet.allOf(User.Fields.class)));
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_nullInfo() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = new User();
        Mockito.when(restClientMock.getUserByInternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getEmail());
        assertNull(user.getId());
        assertNull(user.getWorkContacts());
        assertNull(user.getFiscalCode());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_nullInfo_nullUserResponse() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = null;
        Mockito.when(restClientMock.getUserByInternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertNull(user);

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_certificationNone() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = TestUtils.mockInstance(new User());
        userMock.setId(userId.toString());
        Map<String, WorkContactResource> workContacts = new HashMap<>();
        WorkContactResource workContact = TestUtils.mockInstance(new WorkContactResource());
        workContact.getEmail().setCertification(Certification.NONE);
        userMock.setWorkContacts(workContacts);
        workContacts.put("institutionId", workContact);
        Mockito.when(restClientMock.getUserByInternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertEquals(userId.toString(), user.getId());
        assertEquals(NONE, user.getName().getCertification());
        assertEquals(NONE, user.getEmail().getCertification());
        assertEquals(NONE, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(NONE, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUserByInternalId_nullExternalId() {
        //given
        String userId = null;
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        //when
        Executable executable = () -> userConnector.getUserByInternalId(userId, fieldList);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_certificationNotNone() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = TestUtils.mockInstance(new User());
        userMock.setId(userId.toString());
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
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertEquals(userId.toString(), user.getId());
        assertEquals(SPID, user.getName().getCertification());
        assertEquals(SPID, user.getEmail().getCertification());
        assertEquals(SPID, user.getFamilyName().getCertification());
        user.getWorkContacts().forEach((key1, value) -> assertEquals(SPID, value.getEmail().getCertification()));
        assertNotNull(user.getFiscalCode());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void updateUser() {
        //given
        String institutionId = "institutionId";
        UUID id = UUID.randomUUID();
        MutableUserFieldsDto userDto = TestUtils.mockInstance(new MutableUserFieldsDto(), "setWorkContacts");
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
        MutableUserFieldsDto userDto = TestUtils.mockInstance(new MutableUserFieldsDto(), "setWorkContacts");

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
        SaveUserDto saveUserDto = TestUtils.mockInstance(new SaveUserDto(), "setWorkContacts");
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
        SaveUserDto saveUserDto = TestUtils.mockInstance(new SaveUserDto(), "setWorkContacts");
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