package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.EnumSet;
import java.util.UUID;

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
    void getUserByExternalId_nullInfo_nullCertification() {
        //given
        String externalId = "externalId";
        UserResource userMock = TestUtils.mockInstance(new UserResource());
        userMock.setId(UUID.randomUUID());
        Mockito.when(restClientMock.getUserByExternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId);
        ///then
        assertNull(user.getEmail().getCertification());
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getWorkContact());
        assertNull(user.getFiscalCode());
        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByExternalId(Mockito.eq(EnumSet.of(UserResource.Fields.fiscalCode)), embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByExternalId_nullInfo_nullUserResponse() {
        //given
        String externalId = "externalId";
        UserResource userMock = null;
        Mockito.when(restClientMock.getUserByExternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId);
        ///then
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getWorkContact());
        assertNull(user.getFiscalCode());
        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByExternalId(Mockito.eq(EnumSet.of(UserResource.Fields.fiscalCode)), embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByExternalId_nullInfo_certificationNone() {
        //given
        String externalId = "externalId";
        UserResource userMock = null;
        Mockito.when(restClientMock.getUserByExternalId(Mockito.any(), Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.search(externalId);
        ///then
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getWorkContact());
        assertNull(user.getFiscalCode());

        ArgumentCaptor<EmbeddedExternalId> embeddedCaptor = ArgumentCaptor.forClass(EmbeddedExternalId.class);
//        Mockito.verify(restClientMock, Mockito.times(1))
//                .getUserByExternalId(embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUserByExternalId_nullExternalId() {
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
    void getUserByExternalId_certificationNotNone() {
        //given
        String externalId = "externalId";
        UserResource userResourceMock = TestUtils.mockInstance(new UserResource());
//        userResourceMock.setCertification(Certification.SPID);
//        Mockito.when(restClientMock.getUserByExternalId(Mockito.any()))
//                .thenReturn(userResourceMock);
        //when
        User user = userConnector.search(externalId);
        //then
        assertEquals(userResourceMock.getName(), user.getName());

//        Mockito.verify(restClientMock, Mockito.times(1))
//                .getUserByExternalId(embeddedCaptor.capture());
//        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
//        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_nullInfo_nullCertification() {
        //given
        String userId = "userId";
        UserResource userMock = new UserResource();
//        Mockito.when(restClientMock.getUserByInternalId(Mockito.any()))
//                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId);
        ///then
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getWorkContact());
        assertNull(user.getFiscalCode());

//        Mockito.verify(restClientMock, Mockito.times(1))
//                .getUserByInternalId(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_nullInfo_nullUserResponse() {
        //given
        String userId = "userId";
        UserResource userMock = null;
//        Mockito.when(restClientMock.getUserByInternalId(Mockito.any()))
//                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId);
        ///then
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getWorkContact());
        assertNull(user.getFiscalCode());

//        Mockito.verify(restClientMock, Mockito.times(1))
//                .getUserByInternalId(Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserByInternalId_nullInfo_certificationNone() {
        //given
        String userId = "userId";
        UserResource userMock = new UserResource();
//        Mockito.when(restClientMock.getUserByInternalId(Mockito.any()))
//                .thenReturn(userMock);
        //when
        User user = userConnector.getUserByInternalId(userId);
        ///then
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getFamilyName());
        assertNull(user.getWorkContact());
        assertNull(user.getFiscalCode());

//        Mockito.verify(restClientMock, Mockito.times(1))
//                .getUserByInternalId(Mockito.anyString());
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
        String externalId = "externalId";
        UserResource userResourceMock = TestUtils.mockInstance(new UserResource());
//        Mockito.when(restClientMock.getUserByExternalId(Mockito.any()))
//                .thenReturn(userResourceMock);
        //when
        User user = userConnector.search(externalId);
        //then
        assertEquals(userResourceMock.getId(), user.getId());
        assertEquals(userResourceMock.getName(), user.getName());

//        Mockito.verify(restClientMock, Mockito.times(1))
//                .getUserByExternalId(embeddedCaptor.capture());
//        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
//        assertEquals(externalId, externalIdCaptured.getFiscalCode());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void updateUser() {
        //given
        String institutionId = "institutionId";
        UUID id = UUID.randomUUID();
        MutableUserFieldsDto userDto = TestUtils.mockInstance(new MutableUserFieldsDto());
        //when
        Executable executable = () -> userConnector.updateUser(id, institutionId, userDto);
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
        String institutionId = "institutionId";
        UUID id = null;
        MutableUserFieldsDto userDto = TestUtils.mockInstance(new MutableUserFieldsDto());

        //when
        Executable executable = () -> userConnector.updateUser(id, institutionId, userDto);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A UUID is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void updateUser_nullInstitutionId() {
        //given
        String institutionId = null;
        UUID id = UUID.randomUUID();
        MutableUserFieldsDto userDto = TestUtils.mockInstance(new MutableUserFieldsDto());
        //when
        Executable executable = () -> userConnector.updateUser(id, institutionId, userDto);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An institutionId is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

}