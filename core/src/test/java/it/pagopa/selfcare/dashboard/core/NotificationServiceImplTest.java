package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Configuration;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.config.CoreTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mail.MailPreparationException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.workContacts;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private NotificationServiceConnector notificationConnectorMock;

    @MockBean
    private ProductsConnector productsConnectorMock;

    @SpyBean
    private Configuration freemarkerConfigSpy;

    @MockBean
    private PartyConnector partyConnectorMock;

    @MockBean
    private UserService userServiceMock;

    @Autowired
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<MessageRequest> messageRequestCaptor;

    @Captor
    private ArgumentCaptor<Throwable> throwableCaptor;

    @SpyBean
    private SimpleAsyncUncaughtExceptionHandler simpleAsyncUncaughtExceptionHandler;

    private static Stream<Arguments> getRelationshipBasedNotificationArgumentsProvider() {
        return Stream.of(
                Arguments.of("ACTIVATE", "user_activated.ftlh", "User has been activated", (BiConsumer<NotificationService, String>) NotificationService::sendActivatedUserNotification),
                Arguments.of("DELETE", "user_deleted.ftlh", "User had been deleted", (BiConsumer<NotificationService, String>) NotificationService::sendDeletedUserNotification),
                Arguments.of("SUSPEND", "user_suspended.ftlh", "User has been suspended", (BiConsumer<NotificationService, String>) NotificationService::sendSuspendedUserNotification)
        );
    }


    @Test
    void sendCreatedUserNotification_nullInstitutionId() {
        //given
        String institutionId = null;
        String email = "email";
        String productTitle = "productTitle";
        String productRoles1 = "Operator Api";
        String productRoles2 = "Operator Security";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        PartyRole partyRole2 = PartyRole.SUB_DELEGATE;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        CreateUserDto.Role roleMock2 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        roleMock2.setProductRole(productRoles2);
        roleMock2.setPartyRole(partyRole2);
        Set<CreateUserDto.Role> roles = Set.of(roleMock1, roleMock2);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("Institution id is required", e.getMessage());

        verifyNoInteractions(partyConnectorMock, freemarkerConfigSpy, notificationConnectorMock, productsConnectorMock, userServiceMock);
    }

    @Test
    void sendCreatedUserNotification_nullEmail() {
        //given
        String institutionId = "institutionId";
        String email = null;
        String productTitle = "productTitle";
        String productRoles1 = "Operator Api";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        Set<CreateUserDto.Role> roles = Set.of(roleMock1);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("User email is required", e.getMessage());

        verifyNoInteractions(partyConnectorMock, freemarkerConfigSpy, notificationConnectorMock, productsConnectorMock, userServiceMock);
    }


    @Test
    void sendCreatedUserNotification_nullProductId() {
        //given
        String institutionId = "institutionId";
        String email = "email";
        String productTitle = null;
        String productRoles1 = "Operator Api";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        Set<CreateUserDto.Role> roles = Set.of(roleMock1);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A product Title is required", e.getMessage());
        verifyNoInteractions(partyConnectorMock, freemarkerConfigSpy, notificationConnectorMock, productsConnectorMock, userServiceMock);
    }

    @Test
    void sendCreatedUserNotification_emptyProductRoles() {
        //given
        String institutionId = "institutionId";
        String email = "email";
        String productTitle = "productTitle";

        Set<CreateUserDto.Role> roles = Set.of();
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("ProductRoles are required", e.getMessage());
        verifyNoInteractions(partyConnectorMock, freemarkerConfigSpy, notificationConnectorMock);
    }


    @Test
    void sendCreatedUserNotification_nullInstitutionDescription() {
        //given
        String institutionId = "institutionId";
        String email = "email";
        String productTitle = "productTitle";
        Institution institutionMock = mockInstance(new Institution(), "setDescription");
        String productRoles1 = "Operator Api";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        Set<CreateUserDto.Role> roles = Set.of(roleMock1);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("An institution description is required", e.getMessage());
        verify(partyConnectorMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(freemarkerConfigSpy, notificationConnectorMock, productsConnectorMock, userServiceMock);
    }


    @Test
    void sendCreatedUserNotification_nullAuth() {
        //given
        String institutionId = "institutionId";
        String email = "email";
        String productTitle = "productId";
        Institution institutionMock = mockInstance(new Institution());
        String productRoles1 = "Operator Api";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        Set<CreateUserDto.Role> roles = Set.of(roleMock1);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalStateException.class, e.getClass());
        assertEquals("Authentication is required", e.getMessage());
        verify(partyConnectorMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(freemarkerConfigSpy, notificationConnectorMock, userServiceMock);
    }


    @Test
    void sendCreatedUserNotification_nullPrincipal() {
        //given
        String institutionId = "institutionId";
        String email = "email";
        String productTitle = "productId";
        Institution institutionMock = mockInstance(new Institution());
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(null, null));
        String productRoles1 = "Operator Api";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        Set<CreateUserDto.Role> roles = Set.of(roleMock1);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalStateException.class, e.getClass());
        assertEquals("Not SelfCareUser principal", e.getMessage());
        verify(partyConnectorMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(freemarkerConfigSpy, notificationConnectorMock, productsConnectorMock, userServiceMock);
    }


    @Test
    void sendCreatedUserNotification_MailPreparationException() throws IOException {
        //given
        String institutionId = "institutionId";
        String email = "email";
        String productTitle = "productTitle";
        Institution institutionMock = mockInstance(new Institution());
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        doThrow(RuntimeException.class)
                .when(notificationConnectorMock)
                .sendNotificationToUser(any());
        String productRoles1 = "Operator Api";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        Set<CreateUserDto.Role> roles = Set.of(roleMock1);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(MailPreparationException.class, e.getClass());
        verify(partyConnectorMock, times(1))
                .getInstitution(institutionId);
        verify(freemarkerConfigSpy, times(1))
                .getTemplate("user_added_single_role.ftlh");
        verify(notificationConnectorMock, times(1))
                .sendNotificationToUser(any());
        verifyNoMoreInteractions(partyConnectorMock, notificationConnectorMock);
        verifyNoInteractions(productsConnectorMock, userServiceMock);
    }


    @Test
    void sendCreatedUserNotification_singleRole() throws IOException {
        //given
        String institutionId = "institutionId";
        String email = "email";
        String productTitle = "productTitle";
        Institution institutionMock = mockInstance(new Institution());
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        String productRoles1 = "Operator Api";
        String productLabel = "operator";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setLabel(productLabel);
        roleMock1.setPartyRole(partyRole1);

        Set<CreateUserDto.Role> roles = Set.of(roleMock1);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .getInstitution(institutionId);
        verify(freemarkerConfigSpy, times(1))
                .getTemplate("user_added_single_role.ftlh");
        verify(notificationConnectorMock, times(1))
                .sendNotificationToUser(messageRequestCaptor.capture());
        MessageRequest messageRequest = messageRequestCaptor.getValue();
        assertNotNull(messageRequest);
        assertEquals(email, messageRequest.getReceiverEmail());
        assertEquals("A new user has been added", messageRequest.getSubject());
        assertNotNull(messageRequest.getContent());
        assertTrue(messageRequest.getContent().contains(productTitle));
        roles.forEach(role -> {
            assertTrue(messageRequest.getContent().contains(role.getLabel()));
        });
        assertTrue(messageRequest.getContent().contains(selfCareUser.getUserName()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getSurname()));
        assertTrue(messageRequest.getContent().contains(institutionMock.getDescription()));
    }

    @Test
    void sendCreateUserNotification_multipleRoles() throws IOException {
        //given
        String institutionId = "institutionId";
        String email = "email";
        String productTitle = "productTitle";
        Institution institutionMock = mockInstance(new Institution());
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        String productRoles1 = "Operator Api";
        String productLabel1 = "operator api";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        String productRole2 = "Operator Security";
        String productLabel2 = "operator security";
        PartyRole partyRole2 = PartyRole.SUB_DELEGATE;
        String productRole3 = "Administrator";
        String productLabel3 = "administrator";
        PartyRole partyRole3 = PartyRole.DELEGATE;
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        CreateUserDto.Role roleMock2 = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        CreateUserDto.Role roleMock3 = mockInstance(new CreateUserDto.Role(), "setPartyRole");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        roleMock1.setLabel(productLabel1);
        roleMock2.setProductRole(productRole2);
        roleMock2.setPartyRole(partyRole2);
        roleMock2.setLabel(productLabel2);
        roleMock3.setProductRole(productRole3);
        roleMock3.setPartyRole(partyRole3);
        roleMock3.setLabel(productLabel3);

        Set<CreateUserDto.Role> roles = Set.of(roleMock1, roleMock2, roleMock3);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionId, productTitle, email, roles);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .getInstitution(institutionId);
        verify(freemarkerConfigSpy, times(1))
                .getTemplate("user_added_multi_role.ftlh");
        verify(notificationConnectorMock, times(1))
                .sendNotificationToUser(messageRequestCaptor.capture());
        MessageRequest messageRequest = messageRequestCaptor.getValue();
        assertNotNull(messageRequest);
        assertEquals(email, messageRequest.getReceiverEmail());
        assertEquals("A new user has been added", messageRequest.getSubject());
        assertNotNull(messageRequest.getContent());
        assertTrue(messageRequest.getContent().contains(productTitle));
        roles.forEach(role -> {
            assertTrue(messageRequest.getContent().contains(role.getLabel()));
        });
        assertTrue(messageRequest.getContent().contains(selfCareUser.getUserName()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getSurname()));
        assertTrue(messageRequest.getContent().contains(institutionMock.getDescription()));
        verifyNoMoreInteractions(partyConnectorMock, notificationConnectorMock);
        verifyNoInteractions(productsConnectorMock, userServiceMock);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullRelationshipId(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = null;
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A relationship Id is required", e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, freemarkerConfigSpy, notificationConnectorMock, userServiceMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullEmail(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts");
        userInfo.getUser().setEmail(null);
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("User workContact is required", e.getMessage());
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, freemarkerConfigSpy, notificationConnectorMock);
        verifyNoMoreInteractions(userServiceMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullInstitutionId(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts", "setInstitutionId");
        Institution institutionMock = mockInstance(new Institution(), "setDescription");
        institutionMock.setId(UUID.randomUUID().toString());
        WorkContact workContact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContactsMap = new HashMap<>();
        workContactsMap.put(institutionMock.getId(), workContact);
        userInfo.getUser().setWorkContacts(workContactsMap);
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("An institution id is required", e.getMessage());
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, freemarkerConfigSpy, notificationConnectorMock);
        verifyNoMoreInteractions(userServiceMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullProductId(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts");
        userInfo.setProducts(Map.of("1", mockInstance(new ProductInfo(), "setId")));
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setId(UUID.randomUUID().toString());
        userInfo.setInstitutionId(institutionMock.getId());
        WorkContact workContact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContactsMap = new HashMap<>();
        workContactsMap.put(institutionMock.getId(), workContact);
        userInfo.getUser().setWorkContacts(workContactsMap);
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A product Id is required", e.getMessage());
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, freemarkerConfigSpy, notificationConnectorMock);
        verifyNoMoreInteractions(userServiceMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullInstitutionDescription(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts");
        userInfo.setProducts(Map.of("1", mockInstance(new ProductInfo())));
        Institution institutionMock = mockInstance(new Institution(), "setDescription");
        institutionMock.setId(UUID.randomUUID().toString());
        userInfo.setInstitutionId(institutionMock.getId());
        WorkContact workContact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContactsMap = new HashMap<>();
        workContactsMap.put(institutionMock.getId(), workContact);
        userInfo.getUser().setWorkContacts(workContactsMap);
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("An institution description is required", e.getMessage());
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verify(partyConnectorMock, times(1))
                .getInstitution(userInfo.getInstitutionId());
        verifyNoInteractions(productsConnectorMock, freemarkerConfigSpy, notificationConnectorMock);
        verifyNoMoreInteractions(userServiceMock, partyConnectorMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullProductTitle(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = mockInstance(new ProductInfo());
        userInfo.setProducts(Map.of("1", productInfoMock));
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setId(UUID.randomUUID().toString());
        userInfo.setInstitutionId(institutionMock.getId());
        WorkContact workContact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContactsMap = new HashMap<>();
        workContactsMap.put(institutionMock.getId(), workContact);
        userInfo.getUser().setWorkContacts(workContactsMap);
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setTitle");
        when(productsConnectorMock.getProduct(any()))
                .thenReturn(productMock);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalArgumentException.class, e.getClass());
        assertEquals("A product Title is required", e.getMessage());
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verify(partyConnectorMock, times(1))
                .getInstitution(userInfo.getInstitutionId());
        verify(productsConnectorMock, times(1))
                .getProduct(productInfoMock.getId());
        verifyNoInteractions(freemarkerConfigSpy, notificationConnectorMock);
        verifyNoMoreInteractions(userServiceMock, partyConnectorMock, productsConnectorMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullAuth(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = mockInstance(new ProductInfo());
        userInfo.setProducts(Map.of("1", productInfoMock));
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setId(UUID.randomUUID().toString());
        WorkContact workContact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContactsMap = new HashMap<>();
        workContactsMap.put(institutionMock.getId(), workContact);
        userInfo.getUser().setWorkContacts(workContactsMap);
        userInfo.setInstitutionId(institutionMock.getId());
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        Product productMock = mockInstance(new Product(), "setRoleMappings");
        productMock.setRoleMappings(new EnumMap<>(PartyRole.class));
        when(productsConnectorMock.getProduct(any()))
                .thenReturn(productMock);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalStateException.class, e.getClass());
        assertEquals("Authentication is required", e.getMessage());
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verify(partyConnectorMock, times(1))
                .getInstitution(userInfo.getInstitutionId());
        verify(productsConnectorMock, times(1))
                .getProduct(productInfoMock.getId());
        verifyNoInteractions(freemarkerConfigSpy, notificationConnectorMock);
        verifyNoMoreInteractions(userServiceMock, partyConnectorMock, productsConnectorMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullPrincipal(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = mockInstance(new ProductInfo());
        userInfo.setProducts(Map.of("1", productInfoMock));
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setId(UUID.randomUUID().toString());
        WorkContact workContact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContactsMap = new HashMap<>();
        workContactsMap.put(institutionMock.getId(), workContact);
        userInfo.setInstitutionId(institutionMock.getId());
        userInfo.getUser().setWorkContacts(workContactsMap);
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        Product productMock = mockInstance(new Product(), "setRoleMappings");
        productMock.setRoleMappings(new EnumMap<>(PartyRole.class));
        when(productsConnectorMock.getProduct(any()))
                .thenReturn(productMock);
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(null, null));
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(simpleAsyncUncaughtExceptionHandler, times(1))
                .handleUncaughtException(throwableCaptor.capture(), any(), any());
        Throwable e = throwableCaptor.getValue();
        assertNotNull(e);
        assertEquals(IllegalStateException.class, e.getClass());
        assertEquals("Not SelfCareUser principal", e.getMessage());
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verify(partyConnectorMock, times(1))
                .getInstitution(userInfo.getInstitutionId());
        verify(productsConnectorMock, times(1))
                .getProduct(productInfoMock.getId());
        verifyNoInteractions(freemarkerConfigSpy, notificationConnectorMock);
        verifyNoMoreInteractions(userServiceMock, partyConnectorMock, productsConnectorMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_roleNotFound(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) throws IOException {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = mockInstance(new ProductInfo(), "setRoleInfos");
        productInfoMock.setRoleInfos(List.of(mockInstance(new RoleInfo())));
        userInfo.setProducts(Map.of("1", productInfoMock));
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setId(UUID.randomUUID().toString());
        WorkContact workContact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContactsMap = new HashMap<>();
        workContactsMap.put(institutionMock.getId(), workContact);
        userInfo.setInstitutionId(institutionMock.getId());
        userInfo.getUser().setWorkContacts(workContactsMap);
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        Product productMock = mockInstance(new Product(), "setRoleMappings");
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1),
                mockInstance(new ProductRoleInfo.ProductRole(), 2)));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 3)));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.OPERATOR, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(any()))
                .thenReturn(productMock);
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verify(partyConnectorMock, times(1))
                .getInstitution(userInfo.getInstitutionId());
        verify(productsConnectorMock, times(1))
                .getProduct(productInfoMock.getId());
        verify(freemarkerConfigSpy, times(1))
                .getTemplate(templateName);
        verify(notificationConnectorMock, times(1))
                .sendNotificationToUser(messageRequestCaptor.capture());
        MessageRequest messageRequest = messageRequestCaptor.getValue();
        assertNotNull(messageRequest);
        assertEquals(userInfo.getUser().getWorkContact(institutionMock.getId()).getEmail().getValue(), messageRequest.getReceiverEmail());
        assertEquals(subject, messageRequest.getSubject());
        assertNotNull(messageRequest.getContent());
        assertTrue(messageRequest.getContent().contains(productMock.getTitle()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getUserName()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getSurname()));
        assertTrue(messageRequest.getContent().contains("no_role_found"));
        assertTrue(messageRequest.getContent().contains(institutionMock.getDescription()));
        verifyNoInteractions(simpleAsyncUncaughtExceptionHandler);
        verifyNoMoreInteractions(userServiceMock, partyConnectorMock, productsConnectorMock, notificationConnectorMock);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) throws IOException {
        //given
        String relationshipId = "relationshipId";
        String productRole = "productRole";
        UserInfo userInfo = mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = mockInstance(new ProductInfo(), "setRoleInfos");
        RoleInfo roleInfo = mockInstance(new RoleInfo());
        roleInfo.setRole(productRole);
        productInfoMock.setRoleInfos(List.of(roleInfo));
        userInfo.setProducts(Map.of("1", productInfoMock));
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setId(UUID.randomUUID().toString());
        userInfo.setInstitutionId(institutionMock.getId());
        WorkContact workContact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContactsMap = new HashMap<>();
        workContactsMap.put(institutionMock.getId(), workContact);
        userInfo.getUser().setWorkContacts(workContactsMap);
        when(userServiceMock.findByRelationshipId(any(), any()))
                .thenReturn(userInfo);
        when(partyConnectorMock.getInstitution(any()))
                .thenReturn(institutionMock);
        Product productMock = mockInstance(new Product(), "setRoleMappings");
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole role2 = mockInstance(new ProductRoleInfo.ProductRole(), 2, "setCode");
        role2.setCode(productRole);
        productRoleInfo1.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1),
                role2));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 3)));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.OPERATOR, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(any()))
                .thenReturn(productMock);
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(500);
        };
        //then
        assertDoesNotThrow(executable);
        verify(userServiceMock, times(1))
                .findByRelationshipId(relationshipId, EnumSet.of(workContacts));
        verify(partyConnectorMock, times(1))
                .getInstitution(userInfo.getInstitutionId());
        verify(productsConnectorMock, times(1))
                .getProduct(productInfoMock.getId());
        verify(freemarkerConfigSpy, times(1))
                .getTemplate(templateName);
        verify(notificationConnectorMock, times(1))
                .sendNotificationToUser(messageRequestCaptor.capture());
        MessageRequest messageRequest = messageRequestCaptor.getValue();
        assertNotNull(messageRequest);
        assertEquals(userInfo.getUser().getWorkContact(institutionMock.getId()).getEmail().getValue(), messageRequest.getReceiverEmail());
        assertEquals(subject, messageRequest.getSubject());
        assertNotNull(messageRequest.getContent());
        assertTrue(messageRequest.getContent().contains(productMock.getTitle()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getUserName()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getSurname()));
        assertTrue(messageRequest.getContent().contains(role2.getLabel()));
        assertTrue(messageRequest.getContent().contains(institutionMock.getDescription()));
        verifyNoInteractions(simpleAsyncUncaughtExceptionHandler);
        verifyNoMoreInteractions(userServiceMock, partyConnectorMock, productsConnectorMock, notificationConnectorMock);
    }

}
