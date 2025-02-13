package it.pagopa.selfcare.dashboard.core.utils;

import java.util.Optional;

public class EmailUtils {

    private EmailUtils() {}

    /**
     * <p>Extract the first part of an email address before the at symbol (aka username)</p>
     * <ul>
     * <li>If the email address is null, return null</li>
     * <li>if the email address doesn't contain the at symbol return the string itself</li>
     * </ul>
     *
     * @param email the email address string
     * @return the username
     */
    public static String getUsername(String email) {
        return Optional.ofNullable(email)
                .map(m -> m.indexOf("@"))
                .filter(i -> i > -1)
                .map(i -> email.substring(0, i))
                .orElse(email);
    }

}
