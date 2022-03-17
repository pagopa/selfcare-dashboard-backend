package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Configuration;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.config.CoreTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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

    @SpyBean
    private Configuration freemarkerConfig;

    @MockBean
    private PartyConnector partyConnector;

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
                Arguments.of("ACTIVATE", "activate_referent.ftlh", "User has been activated", (BiConsumer<NotificationService, String>) NotificationService::sendActivatedUserNotification),
                Arguments.of("DELETE", "delete_referent.ftlh", "User had been deleted", (BiConsumer<NotificationService, String>) NotificationService::sendDeletedUserNotification),
                Arguments.of("SUSPEND", "suspend_referent.ftlh", "User has been suspended", (BiConsumer<NotificationService, String>) NotificationService::sendSuspendedUserNotification)
        );
    }


    @Test
    void sendCreatedUserNotification_nullinstitutionExternalId() {
        //given
        String institutionExternalId = null;
        String email = "email";
        String productTitle = "productTitle";
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionExternalId, productTitle, email);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("Institution external id is required", e.getMessage());

        Mockito.verifyNoInteractions(partyConnector, freemarkerConfig, notificationConnector);
    }

    @Test
    void sendCreatedUserNotification_nullEmail() {
        //given
        String institutionExternalId = "institutionExternalId";
        String email = null;
        String productTitle = "productTitle";
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionExternalId, productTitle, email);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("User email is required", e.getMessage());

        Mockito.verifyNoInteractions(partyConnector, freemarkerConfig, notificationConnector);
    }


    @Test
    void sendCreatedUserNotification_nullProductId() {
        //given
        String institutionExternalId = "institutionExternalId";
        String email = "email";
        String productTitle = null;
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionExternalId, productTitle, email);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("A product Title is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnector, freemarkerConfig, notificationConnector);
    }


    @Test
    void sendCreatedUserNotification_nullInstitutionDescription() {
        //given
        String institutionExternalId = "institutionExternalId";
        String email = "email";
        String productTitle = "productTitle";
        Institution institutionMock = TestUtils.mockInstance(new Institution(), "setDescription");
        Mockito.when(partyConnector.getInstitutionByExternalId(Mockito.any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionExternalId, productTitle, email);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("An institution description is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitutionByExternalId(institutionExternalId);
        Mockito.verifyNoInteractions(freemarkerConfig, notificationConnector);
    }


    @Test
    void sendCreatedUserNotification_nullAuth() {
        //given
        String institutionExternalId = "institutionExternalId";
        String email = "email";
        String productTitle = "productId";
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitutionByExternalId(Mockito.any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionExternalId, productTitle, email);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalStateException.class, e.getClass());
        Assertions.assertEquals("Authentication is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitutionByExternalId(institutionExternalId);
        Mockito.verifyNoInteractions(freemarkerConfig, notificationConnector);
    }


    @Test
    void sendCreatedUserNotification_nullPrincipal() {
        //given
        String institutionExternalId = "institutionExternalId";
        String email = "email";
        String productTitle = "productId";
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitutionByExternalId(Mockito.any()))
                .thenReturn(institutionMock);
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(null, null));
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionExternalId, productTitle, email);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalStateException.class, e.getClass());
        Assertions.assertEquals("Not SelfCareUser principal", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitutionByExternalId(institutionExternalId);
        Mockito.verifyNoInteractions(freemarkerConfig, notificationConnector);
    }


    @Test
    void sendCreatedUserNotification_MailPreparationException() throws IOException {
        //given
        String institutionExternalId = "institutionExternalId";
        String email = "email";
        String productTitle = "productTitle";
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitutionByExternalId(Mockito.any()))
                .thenReturn(institutionMock);
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        Mockito.doThrow(RuntimeException.class)
                .when(notificationConnector)
                .sendNotificationToUser(Mockito.any());
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionExternalId, productTitle, email);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(MailPreparationException.class, e.getClass());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitutionByExternalId(institutionExternalId);
        Mockito.verify(freemarkerConfig, Mockito.times(1))
                .getTemplate("add_referent.ftlh");
        Mockito.verify(notificationConnector, Mockito.times(1))
                .sendNotificationToUser(Mockito.any());
    }


    @Test
    void sendCreatedUserNotification() throws IOException {
        //given
        String institutionExternalId = "institutionExternalId";
        String email = "email";
        String productTitle = "productTitle";
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitutionByExternalId(Mockito.any()))
                .thenReturn(institutionMock);
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(selfCareUser, null));
        //when
        Executable executable = () -> {
            notificationService.sendCreatedUserNotification(institutionExternalId, productTitle, email);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitutionByExternalId(institutionExternalId);
        Mockito.verify(freemarkerConfig, Mockito.times(1))
                .getTemplate("add_referent.ftlh");
        Mockito.verify(notificationConnector, Mockito.times(1))
                .sendNotificationToUser(messageRequestCaptor.capture());
        MessageRequest messageRequest = messageRequestCaptor.getValue();
        assertNotNull(messageRequest);
        assertEquals(email, messageRequest.getReceiverEmail());
        assertEquals("A new user has been added", messageRequest.getSubject());
        assertNotNull(messageRequest.getContent());
        assertTrue(messageRequest.getContent().contains(productTitle));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getUserName()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getSurname()));
        assertTrue(messageRequest.getContent().contains(institutionMock.getDescription()));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullRelationshipId(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = null;
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("A relationship Id is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnector, productsConnector, freemarkerConfig, notificationConnector);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullEmail(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts", "setEmail");
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("User email is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verifyNoInteractions(productsConnector, freemarkerConfig, notificationConnector);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullInstitutionId(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts", "setInstitutionId");
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("An institution id is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verifyNoInteractions(productsConnector, freemarkerConfig, notificationConnector);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullProductId(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts");
        userInfo.setProducts(Map.of("1", TestUtils.mockInstance(new ProductInfo(), "setId")));
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("A product Id is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verifyNoInteractions(productsConnector, freemarkerConfig, notificationConnector);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullInstitutionDescription(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts");
        userInfo.setProducts(Map.of("1", TestUtils.mockInstance(new ProductInfo())));
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        Institution institutionMock = TestUtils.mockInstance(new Institution(), "setDescription");
        Mockito.when(partyConnector.getInstitution(Mockito.any()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("An institution description is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitution(userInfo.getInstitutionId());
        Mockito.verifyNoInteractions(productsConnector, freemarkerConfig, notificationConnector);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullProductTitle(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        userInfo.setProducts(Map.of("1", productInfoMock));
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitution(Mockito.any()))
                .thenReturn(institutionMock);
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings", "setTitle");
        Mockito.when(productsConnector.getProduct(Mockito.any()))
                .thenReturn(productMock);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalArgumentException.class, e.getClass());
        Assertions.assertEquals("A product Title is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitution(userInfo.getInstitutionId());
        Mockito.verify(productsConnector, Mockito.times(1))
                .getProduct(productInfoMock.getId());
        Mockito.verifyNoInteractions(freemarkerConfig, notificationConnector);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullAuth(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        userInfo.setProducts(Map.of("1", productInfoMock));
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitution(Mockito.any()))
                .thenReturn(institutionMock);
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
        productMock.setRoleMappings(new EnumMap<>(PartyRole.class));
        Mockito.when(productsConnector.getProduct(Mockito.any()))
                .thenReturn(productMock);
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalStateException.class, e.getClass());
        Assertions.assertEquals("Authentication is required", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitution(userInfo.getInstitutionId());
        Mockito.verify(productsConnector, Mockito.times(1))
                .getProduct(productInfoMock.getId());
        Mockito.verifyNoInteractions(freemarkerConfig, notificationConnector);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_nullPrincipal(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        userInfo.setProducts(Map.of("1", productInfoMock));
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitution(Mockito.any()))
                .thenReturn(institutionMock);
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
        productMock.setRoleMappings(new EnumMap<>(PartyRole.class));
        Mockito.when(productsConnector.getProduct(Mockito.any()))
                .thenReturn(productMock);
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(null, null));
        //when
        Executable executable = () -> {
            consumer.accept(notificationService, relationshipId);
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(simpleAsyncUncaughtExceptionHandler, Mockito.times(1))
                .handleUncaughtException(throwableCaptor.capture(), Mockito.any(), Mockito.any());
        Throwable e = throwableCaptor.getValue();
        Assertions.assertNotNull(e);
        Assertions.assertEquals(IllegalStateException.class, e.getClass());
        Assertions.assertEquals("Not SelfCareUser principal", e.getMessage());
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitution(userInfo.getInstitutionId());
        Mockito.verify(productsConnector, Mockito.times(1))
                .getProduct(productInfoMock.getId());
        Mockito.verifyNoInteractions(freemarkerConfig, notificationConnector);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification_roleNotFound(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) throws IOException {
        //given
        String relationshipId = "relationshipId";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo(), "setRoleInfos");
        productInfoMock.setRoleInfos(List.of(TestUtils.mockInstance(new RoleInfo())));
        userInfo.setProducts(Map.of("1", productInfoMock));
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitution(Mockito.any()))
                .thenReturn(institutionMock);
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
        ProductRoleInfo productRoleInfo1 = TestUtils.mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1),
                TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 2)));
        ProductRoleInfo productRoleInfo2 = TestUtils.mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of(TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 3)));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.OPERATOR, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        Mockito.when(productsConnector.getProduct(Mockito.any()))
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
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitution(userInfo.getInstitutionId());
        Mockito.verify(productsConnector, Mockito.times(1))
                .getProduct(productInfoMock.getId());
        Mockito.verify(freemarkerConfig, Mockito.times(1))
                .getTemplate(templateName);
        Mockito.verify(notificationConnector, Mockito.times(1))
                .sendNotificationToUser(messageRequestCaptor.capture());
        MessageRequest messageRequest = messageRequestCaptor.getValue();
        assertNotNull(messageRequest);
        assertEquals(userInfo.getEmail(), messageRequest.getReceiverEmail());
        assertEquals(subject, messageRequest.getSubject());
        assertNotNull(messageRequest.getContent());
        assertTrue(messageRequest.getContent().contains(productMock.getTitle()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getUserName()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getSurname()));
        assertTrue(messageRequest.getContent().contains("no_role_found"));
        assertTrue(messageRequest.getContent().contains(institutionMock.getDescription()));
        Mockito.verifyNoInteractions(simpleAsyncUncaughtExceptionHandler);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getRelationshipBasedNotificationArgumentsProvider")
    void sendRelationshipBasedNotification(String argType, String templateName, String subject, BiConsumer<NotificationService, String> consumer) throws IOException {
        //given
        String relationshipId = "relationshipId";
        String productRole = "productRole";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo(), "setProducts");
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo(), "setRoleInfos");
        RoleInfo roleInfo = TestUtils.mockInstance(new RoleInfo());
        roleInfo.setRole(productRole);
        productInfoMock.setRoleInfos(List.of(roleInfo));
        userInfo.setProducts(Map.of("1", productInfoMock));
        Mockito.when(partyConnector.getUser(Mockito.any()))
                .thenReturn(userInfo);
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(partyConnector.getInstitution(Mockito.any()))
                .thenReturn(institutionMock);
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
        ProductRoleInfo productRoleInfo1 = TestUtils.mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole role2 = TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 2, "setCode");
        role2.setCode(productRole);
        productRoleInfo1.setRoles(List.of(TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1),
                role2));
        ProductRoleInfo productRoleInfo2 = TestUtils.mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of(TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 3)));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.OPERATOR, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        Mockito.when(productsConnector.getProduct(Mockito.any()))
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
            Thread.sleep(1000);
        };
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verify(partyConnector, Mockito.times(1))
                .getInstitution(userInfo.getInstitutionId());
        Mockito.verify(productsConnector, Mockito.times(1))
                .getProduct(productInfoMock.getId());
        Mockito.verify(freemarkerConfig, Mockito.times(1))
                .getTemplate(templateName);
        Mockito.verify(notificationConnector, Mockito.times(1))
                .sendNotificationToUser(messageRequestCaptor.capture());
        MessageRequest messageRequest = messageRequestCaptor.getValue();
        assertNotNull(messageRequest);
        assertEquals(userInfo.getEmail(), messageRequest.getReceiverEmail());
        assertEquals(subject, messageRequest.getSubject());
        assertNotNull(messageRequest.getContent());
        assertTrue(messageRequest.getContent().contains(productMock.getTitle()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getUserName()));
        assertTrue(messageRequest.getContent().contains(selfCareUser.getSurname()));
        assertTrue(messageRequest.getContent().contains(role2.getLabel()));
        assertTrue(messageRequest.getContent().contains(institutionMock.getDescription()));
        Mockito.verifyNoInteractions(simpleAsyncUncaughtExceptionHandler);
    }

}