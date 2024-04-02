package it.pagopa.selfcare.dashboard.core.exception;

public class InvalidOnboardingStatusException extends RuntimeException {

    public InvalidOnboardingStatusException(String message) {
        super(message);
    }

    public InvalidOnboardingStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
