package it.pagopa.selfcare.dashboard.web.exception;

import it.pagopa.selfcare.dashboard.connector.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class InvalidRequestExceptionTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link InvalidRequestException#InvalidRequestException(String, String)}
     *   <li>{@link InvalidRequestException#getCode()}
     * </ul>
     */
    @Test
    void testConstructor() {
        assertEquals("Code", (new InvalidRequestException("An error occurred", "Code")).getCode());
    }
}

