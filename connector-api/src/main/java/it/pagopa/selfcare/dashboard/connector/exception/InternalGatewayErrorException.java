package it.pagopa.selfcare.dashboard.connector.exception;

public class InternalGatewayErrorException extends RuntimeException {
    public InternalGatewayErrorException() {
    }

    public InternalGatewayErrorException(String message) {
        super(message);
    }
}
