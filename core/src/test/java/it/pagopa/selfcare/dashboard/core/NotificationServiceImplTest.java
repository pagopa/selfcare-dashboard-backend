package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.TestSecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    @Mock
    private NotificationServiceConnector notificationConnector;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void sendNotification_nullEmail() {
        //given
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        String email = null;
        String productTitle = "productTitle";
        //when
        Executable executable = () -> notificationService.sendNotificationCreateUserRelationship(productTitle, email);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("User email is required", e.getMessage());
        Mockito.verifyNoInteractions(notificationConnector);
    }

    @Test
    void sendNotification_nullProductId() {
        //given
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        String email = "email";
        String productTitle = null;
        //when
        Executable executable = () -> notificationService.sendNotificationCreateUserRelationship(productTitle, email);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A product Title is required", e.getMessage());
        Mockito.verifyNoInteractions(notificationConnector);
    }

    @Test
    void sendNotification_nullPrincipal() {
        //given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        TestSecurityContextHolder.setAuthentication(authentication);
        String email = "email";
        String productTitle = "productId";
        //when
        Executable executable = () -> notificationService.sendNotificationCreateUserRelationship(productTitle, email);
        //then
        IllegalStateException illegalStateException = Assertions.assertThrows(IllegalStateException.class, executable);
        Assertions.assertEquals("Not SelfCareUser principal", illegalStateException.getMessage());
        Mockito.verifyNoInteractions(notificationConnector);
    }

    @Test
    void sendNotification_nullAuth() {
        //given
        String email = null;
        String productTitle = null;
        //when
        Executable executable = () -> notificationService.sendNotificationCreateUserRelationship(productTitle, email);
        //then
        IllegalStateException illegalStateException = Assertions.assertThrows(IllegalStateException.class, executable);
        Assertions.assertEquals("Authentication is required", illegalStateException.getMessage());
        Mockito.verifyNoInteractions(notificationConnector);

    }

}
