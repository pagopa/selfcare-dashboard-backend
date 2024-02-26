package it.pagopa.selfcare.dashboard.core;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import it.pagopa.selfcare.dashboard.connector.model.support.UserField;
import it.pagopa.selfcare.dashboard.connector.model.user.CertifiedField;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SupportServiceImplTest {

    @Mock
    private UserRegistryConnector userRegistryConnector;

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequest(){
        FreeMarkerConfigurer freeMarkerConfigurer = freemarkerClassLoaderConfig();
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "test-organization", "redirectUrl", freeMarkerConfigurer, null);
        String url = supportServiceImpl.sendRequest(this.dummySupportRequest());
        assertNotNull(url);
        assertTrue(url.contains("jwt"));
        assertEquals("<html>", url.substring(0, 6));
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestWithUserId(){
        FreeMarkerConfigurer freeMarkerConfigurer = freemarkerClassLoaderConfig();
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "test-organization", "redirectUrl", freeMarkerConfigurer, userRegistryConnector);
        SupportRequest supportRequest = this.dummySupportRequest();
        supportRequest.setUserId("userId");
        supportRequest.setInstitutionId("institutionId");
        User user = new User();
        user.setFiscalCode("fiscalCode");
        CertifiedField<String> certifiedField = new CertifiedField<>();
        certifiedField.setValue("name");
        user.setName(certifiedField);
        user.setFamilyName(certifiedField);
        when(userRegistryConnector.getUserByInternalId(anyString(), any())).thenReturn(user);
        String url = supportServiceImpl.sendRequest(supportRequest);
        assertNotNull(url);
        assertTrue(url.contains("jwt"));
        assertEquals("<html>", url.substring(0, 6));
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestUserNotFound(){
        FreeMarkerConfigurer freeMarkerConfigurer = freemarkerClassLoaderConfig();
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "test-organization", "redirectUrl", freeMarkerConfigurer, null);
        SupportRequest supportRequest = this.dummySupportRequest();
        supportRequest.setUserId("null");
        Executable executable = () -> supportServiceImpl.sendRequest(supportRequest);
        // then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        Assertions.assertEquals("User with id null not found", e.getMessage());
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestThrowException() {
        FreeMarkerConfigurer freeMarkerConfigurer = Mockito.mock(FreeMarkerConfigurer.class);
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "test-organization", "redirectUrl", freeMarkerConfigurer, null);
        Exception exception = assertThrows(Exception.class, () -> supportServiceImpl.sendRequest(this.dummySupportRequest()));
        assertEquals("Impossible to retrieve zendesk form template", exception.getMessage());
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestWithProductId() {
        FreeMarkerConfigurer freeMarkerConfigurer = freemarkerClassLoaderConfig();
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "", "redirectUrl", freeMarkerConfigurer, null);
        SupportRequest supportRequest = this.dummySupportRequest();
        supportRequest.setProductId("prodottoDiTest");
        String url = supportServiceImpl.sendRequest(supportRequest);
        assertNotNull(url);
        assertTrue(url.contains("jwt"));
        assertTrue(url.contains(supportRequest.getProductId()));
        assertEquals("<html>", url.substring(0, 6));
    }
    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testRequestWithMalformedEmptyKey() {
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("", "", "test", "redirectUrl", null, null);
        SupportException exception = assertThrows(SupportException.class, () -> supportServiceImpl.sendRequest(this.dummySupportRequest()));
        assertEquals("secret key byte array cannot be null or empty.", exception.getMessage());
    }

    private SupportRequest dummySupportRequest() {
        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setName("Mario Rossi");
        supportRequest.setEmail("test@gmail.com");
        UserField userField = new UserField();
        userField.setAux_data("PPL89M");
        supportRequest.setUserFields(userField);
        return supportRequest;
    }
    private FreeMarkerConfigurer freemarkerClassLoaderConfig() {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_27);
        TemplateLoader templateLoader = new ClassTemplateLoader(this.getClass(), "/template-zendesk");
        configuration.setTemplateLoader(templateLoader);
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setConfiguration(configuration);
        return freeMarkerConfigurer;
    }
}
