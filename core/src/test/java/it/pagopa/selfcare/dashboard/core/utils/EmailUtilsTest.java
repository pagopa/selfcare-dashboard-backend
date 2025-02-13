package it.pagopa.selfcare.dashboard.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmailUtilsTest {

    @Test
    void getUsernameTest() {
        assertEquals("test", EmailUtils.getUsername("test@test.com"));
        assertEquals("test", EmailUtils.getUsername("test"));
        assertEquals("", EmailUtils.getUsername("@test.com"));
        assertNull(EmailUtils.getUsername(null));
    }

}
