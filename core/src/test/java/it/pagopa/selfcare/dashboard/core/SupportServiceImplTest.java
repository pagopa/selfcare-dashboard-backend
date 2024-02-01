package it.pagopa.selfcare.dashboard.core;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import it.pagopa.selfcare.dashboard.connector.model.support.UserField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class SupportServiceImplTest {

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequest(){
        FreeMarkerConfigurer freeMarkerConfigurer = freemarkerClassLoaderConfig();
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "test-organization", freeMarkerConfigurer);
        String url = supportServiceImpl.sendRequest(this.dummySupportRequest());
        assertNotNull(url);
        assertTrue(url.contains("jwt"));
        assertEquals("<html>", url.substring(0, 6));
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestThrowException() {
        FreeMarkerConfigurer freeMarkerConfigurer = Mockito.mock(FreeMarkerConfigurer.class);
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "test-organization", freeMarkerConfigurer);
        Exception exception = assertThrows(Exception.class, () -> supportServiceImpl.sendRequest(this.dummySupportRequest()));
        assertEquals("Impossible to retrieve zendesk form template", exception.getMessage());
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestWithProductId() {
        FreeMarkerConfigurer freeMarkerConfigurer = freemarkerClassLoaderConfig();
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "", freeMarkerConfigurer);
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
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("", "", "test", null);
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
