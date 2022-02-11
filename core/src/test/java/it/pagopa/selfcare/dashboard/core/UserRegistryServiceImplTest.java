package it.pagopa.selfcare.dashboard.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.pagopa.selfcare.commons.base.TargetEnvironment;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
class UserRegistryServiceImplTest {

    @Mock
    private UserRegistryConnector userConnectorMock;

    @InjectMocks
    private UserRegistryServiceImpl userRegistryService;


    @SystemStub
    private EnvironmentVariables environmentVariables;

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
    void getUser_doNotLog() {
        //given
        environmentVariables.set("ENV_TARGET", TargetEnvironment.PROD);
        String externalId = "externalId";
        User expectedUser = new User();
        Mockito.when(userConnectorMock.getUser(Mockito.any()))
                .thenReturn(expectedUser);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(listAppender);
        rootLogger.setLevel(Level.DEBUG);
        //when
        User user = userRegistryService.getUser(externalId);
        //then
        Assertions.assertEquals(0, listAppender.list.stream()
                .filter(iLoggingEvent -> Level.DEBUG.equals(iLoggingEvent.getLevel())
                        && UserRegistryServiceImpl.class.getName().equals(iLoggingEvent.getLoggerName()))
                .count());

    }

    @Test
    void getUser_doLog() {
        //given
        String externalId = "externalId";
        User expectedUser = new User();
        Mockito.when(userConnectorMock.getUser(Mockito.any()))
                .thenReturn(expectedUser);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(listAppender);
        rootLogger.setLevel(Level.DEBUG);
        //when
        User user = userRegistryService.getUser(externalId);
        //then

        Assertions.assertEquals(2, listAppender.list.stream()
                .filter(iLoggingEvent -> Level.DEBUG.equals(iLoggingEvent.getLevel())
                        && UserRegistryServiceImpl.class.getName().equals(iLoggingEvent.getLoggerName()))
                .count());
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