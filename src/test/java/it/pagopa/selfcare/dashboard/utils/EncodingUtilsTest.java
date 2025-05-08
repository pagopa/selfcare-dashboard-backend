package it.pagopa.selfcare.dashboard.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncodingUtilsTest {

    @Test
    void isUrlEncodedTest() {
        assertTrue(EncodingUtils.isUrlEncoded(""));
        assertTrue(EncodingUtils.isUrlEncoded(null));
        assertTrue(EncodingUtils.isUrlEncoded("file%3A%2F%2F%2Fhome%2Fuser"));
        assertTrue(EncodingUtils.isUrlEncoded("%E2%9C%93"));
        assertTrue(EncodingUtils.isUrlEncoded("%7B%22key%22%3A123%7D"));
        assertTrue(EncodingUtils.isUrlEncoded("email%40example.com"));
        assertTrue(EncodingUtils.isUrlEncoded("hello%20world"));
        assertTrue(EncodingUtils.isUrlEncoded("hello+world"));
        assertFalse(EncodingUtils.isUrlEncoded("{\"key\":123}"));
        assertFalse(EncodingUtils.isUrlEncoded("✓"));
        assertFalse(EncodingUtils.isUrlEncoded("%ZZ"));
        assertFalse(EncodingUtils.isUrlEncoded("%2"));
        assertFalse(EncodingUtils.isUrlEncoded("hello%"));
        assertFalse(EncodingUtils.isUrlEncoded("%"));
        assertFalse(EncodingUtils.isUrlEncoded("100%"));
        assertFalse(EncodingUtils.isUrlEncoded("50% off"));
        assertFalse(EncodingUtils.isUrlEncoded("hello%2Gworld"));
        assertFalse(EncodingUtils.isUrlEncoded("café"));
        assertFalse(EncodingUtils.isUrlEncoded("hello world"));
    }

}
