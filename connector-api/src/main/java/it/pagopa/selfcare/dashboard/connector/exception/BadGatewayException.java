package it.pagopa.selfcare.dashboard.connector.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message) {
        super(message);
    }
}
