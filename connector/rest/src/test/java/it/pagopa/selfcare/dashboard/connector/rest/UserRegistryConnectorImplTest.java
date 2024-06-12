package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.SaveUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserRegistryConnectorImplTest extends BaseConnectorTest {

    @InjectMocks
    private UserRegistryConnectorImpl userConnector;

    @Mock
    private UserRegistryRestClient restClientMock;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

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
        assertEquals(user, userMock);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(new EmbeddedExternalId(externalId), EnumSet.allOf(User.Fields.class));
    }

    @Test
    void search_nullInfo_nullUserResponse() {
        //given
        String externalId = "externalId";
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        Mockito.when(restClientMock.search(new EmbeddedExternalId(externalId), fieldList))
                .thenReturn(null);
        //when
        User user = userConnector.search(externalId, fieldList);
        ///then
        assertNull(user);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(new EmbeddedExternalId(externalId), EnumSet.allOf(User.Fields.class));
    }


    @Test
    void search_certificationNone() throws IOException {
        //given
        String externalId = "externalId";
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        ClassPathResource userResource = new ClassPathResource("stubs/UserCertificationNone.json");
        byte[] userResourceStream = Files.readAllBytes(userResource.getFile().toPath());
        User userMock = objectMapper.readValue(userResourceStream, new TypeReference<>() {});

        Mockito.when(restClientMock.search(new EmbeddedExternalId(externalId), fieldList))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId, fieldList);
        ///then
        assertEquals(userMock,user);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(new EmbeddedExternalId(externalId), EnumSet.allOf(User.Fields.class));
    }


    @Test
    void search_nullExternalId() {
        //given
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        //when
        Executable executable = () -> userConnector.search(null, fieldList);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A TaxCode is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void search_certificationNotNone() throws IOException {
        //given
        String externalId = "externalId";
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        ClassPathResource userResource = new ClassPathResource("stubs/UserCertificationNotNone.json");
        byte[] userResourceStream = Files.readAllBytes(userResource.getFile().toPath());
        User userMock = objectMapper.readValue(userResourceStream, new TypeReference<>() {});

        Mockito.when(restClientMock.search(new EmbeddedExternalId(externalId), fieldList))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId, fieldList);
        //then
        assertEquals(user, userMock);
        Mockito.verify(restClientMock, Mockito.times(1))
                .search(new EmbeddedExternalId(externalId), EnumSet.allOf(User.Fields.class));
    }

    @Test
    void getUserByInternalId_nullInfo() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        User userMock = new User();
        Mockito.when(restClientMock.getUserByInternalId(userId, fieldList))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertEquals(user, userMock);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
    }

    @Test
    void getUserByInternalId_nullInfo_nullUserResponse() {
        //given
        UUID userId = UUID.randomUUID();
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        Mockito.when(restClientMock.getUserByInternalId(userId, fieldList))
                .thenReturn(null);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertNull(user);
    }

    @Test
    void getUserByInternalId_certificationNone() throws IOException {
        //given
        UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        ClassPathResource userResource = new ClassPathResource("stubs/UserCertificationNone.json");
        byte[] userResourceStream = Files.readAllBytes(userResource.getFile().toPath());
        User userMock = objectMapper.readValue(userResourceStream, new TypeReference<>() {});

        Mockito.when(restClientMock.getUserByInternalId(userId, fieldList))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertEquals(user, userMock);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
    }


    @Test
    void getUserByInternalId_nullExternalId() {
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);
        //when
        Executable executable = () -> userConnector.getUserByInternalId(null, fieldList);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A userId is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_certificationNotNone() throws IOException {
        //given
        UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        final EnumSet<User.Fields> fieldList = EnumSet.allOf(User.Fields.class);

        ClassPathResource userResource = new ClassPathResource("stubs/UserCertificationNotNone.json");
        byte[] userResourceStream = Files.readAllBytes(userResource.getFile().toPath());
        User userMock = objectMapper.readValue(userResourceStream, new TypeReference<>() {});

        Mockito.when(restClientMock.getUserByInternalId(userId, fieldList))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId.toString(), fieldList);
        ///then
        assertEquals(user, userMock);

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByInternalId(userId, EnumSet.allOf(User.Fields.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void updateUser() {
        //given
        UUID id = UUID.randomUUID();
        MutableUserFieldsDto userDto = new MutableUserFieldsDto();
        userDto.setWorkContacts(new HashMap<>());
        //when
        Executable executable = () -> userConnector.updateUser(id, userDto);
        //then
        assertDoesNotThrow(executable);
        ArgumentCaptor<MutableUserFieldsDto> userDtoCaptor = ArgumentCaptor.forClass(MutableUserFieldsDto.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .patchUser(Mockito.any(), userDtoCaptor.capture());

        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void updateUser_nullId() {
        MutableUserFieldsDto userDto = new MutableUserFieldsDto();
        userDto.setWorkContacts(new HashMap<>());
        //when
        Executable executable = () -> userConnector.updateUser(null, userDto);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A UUID is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void saveUser() {
        //given
        UserId id = new UserId();
        SaveUserDto saveUserDto = new SaveUserDto();
        saveUserDto.setWorkContacts(new HashMap<>());
        Mockito.when(restClientMock.saveUser(saveUserDto))
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