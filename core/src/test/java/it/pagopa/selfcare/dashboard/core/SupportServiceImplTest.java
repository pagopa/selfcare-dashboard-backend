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
    @Test
    void testSendRequest() {
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26");
        String url = supportServiceImpl.sendRequest(this.dummySupportRequest());
        assertNotNull(url);
        assertTrue(url.contains("jwt"));
        assertEquals("http", url.substring(0, 4));
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testRequestWithMalformedApiKey() {
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("pp");
        SupportException exception = assertThrows(SupportException.class, () -> supportServiceImpl.sendRequest(this.dummySupportRequest()));
        assertEquals("The secret length must be at least 256 bits", exception.getMessage());
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
