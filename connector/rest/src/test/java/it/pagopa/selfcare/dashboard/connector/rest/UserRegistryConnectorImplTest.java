package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.Certification;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserDto;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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

    @Captor
    private ArgumentCaptor<EmbeddedExternalId> embeddedCaptor;

    @Test
    void getUser_nullInfo_nullCertification() {
        //given
        String externalId = "externalId";
        UserResponse userMock = new UserResponse();
        userMock.setCertification(null);
        Mockito.when(restClientMock.getUserByExternalId(Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUser(externalId);
        ///then
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertFalse(user.isCertification());
        assertNull(user.getSurname());
        assertNull(user.getFiscalCode());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByExternalId(embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getExternalId());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUser_nullInfo_nullUserResponse() {
        //given
        String externalId = "externalId";
        UserResponse userMock = null;
        Mockito.when(restClientMock.getUserByExternalId(Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUser(externalId);
        ///then
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertFalse(user.isCertification());
        assertNull(user.getSurname());
        assertNull(user.getFiscalCode());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByExternalId(embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getExternalId());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUser_nullInfo_certificationNone() {
        //given
        String externalId = "externalId";
        UserResponse userMock = new UserResponse();
        userMock.setCertification(Certification.NONE);
        Mockito.when(restClientMock.getUserByExternalId(Mockito.any()))
                .thenReturn(userMock);
        //when
        User user = userConnector.getUser(externalId);
        ///then
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertFalse(user.isCertification());
        assertNull(user.getSurname());
        assertNull(user.getFiscalCode());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByExternalId(embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getExternalId());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUser_nullExternalId() {
        //given
        String externalId = null;
        //when
        Executable executable = () -> userConnector.getUser(externalId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A TaxCode is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUser_certificationNotNone() {
        //given
        String externalId = "externalId";
        UserResponse userResponseMock = TestUtils.mockInstance(new UserResponse());
        userResponseMock.setCertification(Certification.SPID);
        Mockito.when(restClientMock.getUserByExternalId(Mockito.any()))
                .thenReturn(userResponseMock);
        //when
        User user = userConnector.getUser(externalId);
        //then
        assertTrue(user.isCertification());
        assertEquals(userResponseMock.getName(), user.getName());
        assertEquals(userResponseMock.getSurname(), user.getSurname());
        assertEquals(userResponseMock.getExtras().getEmail(), user.getEmail());
        assertEquals(userResponseMock.getExternalId(), user.getFiscalCode());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserByExternalId(embeddedCaptor.capture());
        EmbeddedExternalId externalIdCaptured = embeddedCaptor.getValue();
        assertEquals(externalId, externalIdCaptured.getExternalId());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void updateUser() {
        //given
        String institutionId = "institutionId";
        UUID id = UUID.randomUUID();
        UserDto userDto = TestUtils.mockInstance(new UserDto());
        //when
        Executable executable = () -> userConnector.saveUser(id, institutionId, userDto);
        //then
        assertDoesNotThrow(executable);
    }

}