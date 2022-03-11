package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.core.config.CoreTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NotificationServiceImpl.class, CoreTestConfig.class})
@TestPropertySource(
        properties = "PUBLIC_FILE_STORAGE_BASE_URL:https://selcdcheckoutsa.z6.web.core.windows.net/resources/templates/email/"
)
class NotificationServiceImplTest {

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    @MockBean
    private NotificationServiceConnector notificationConnector;

    @MockBean
    private ProductsConnector productsConnector;

    @MockBean
    private Configuration freemarkerConfig;

    @MockBean
    private PartyConnector partyConnector;

    @Autowired
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
    void sendNotificationCreateUser_nullProductId() {
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
    void sendNotificationCreateUser_nullPrincipal() {
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
    void sendNotificationCreateUser_nullAuth() {
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

    @Test
    void sendNotificationCreateUser() throws IOException {
        //given
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        String templateName = "add_referent.ftlh";
        Template template = freemarkerConfig.getTemplate(templateName);
        Authentication authentication = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authentication);
        String email = "email";
        String productTitle = "productTitle";
        Mockito.when(freemarkerConfig.getTemplate(templateName)).thenReturn(template);
        //when
        Executable executable = () -> notificationService.sendNotificationCreateUserRelationship(productTitle, email);
        //then
        assertDoesNotThrow(executable);

    }

}
