package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import it.pagopa.selfcare.dashboard.connector.model.support.UserField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class SupportServiceImplTest {


    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    /*@Test
    void testSendRequest() {
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "test-organization", null);
        String url = supportServiceImpl.sendRequest(this.dummySupportRequest());
        assertNotNull(url);
        assertTrue(url.contains("jwt"));
        assertEquals("http", url.substring(0, 4));
    }*/

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    /*@Test
    void testSendRequestWithProductId() {
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "", null);
        SupportRequest supportRequest = this.dummySupportRequest();
        supportRequest.setProductId("prodottoDiTest");
        String url = supportServiceImpl.sendRequest(supportRequest);
        assertNotNull(url);
        assertTrue(url.contains("jwt"));
        assertTrue(url.contains(supportRequest.getProductId()));
        assertEquals("http", url.substring(0, 4));
    }*/

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
}
