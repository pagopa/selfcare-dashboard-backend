package it.pagopa.selfcare.dashboard.web.exception;

import it.pagopa.selfcare.dashboard.connector.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("Invalid Request", (new InvalidRequestException("Invalid Request")).getMessage());
    }
}

