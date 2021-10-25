package it.pagopa.selfcare.dashboard.web.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The Class ErrorResource.
 */
@Data
@NoArgsConstructor
public class ErrorResource implements Serializable {

    /**
     * The message.
     */
    private String message;

    /**
     * The message title.
     */
    private String messageTitle;

    /**
     * The message key.
     */
    private String messageKey;

    /**
     * The error code.
     */
    private String errorCode;

    public ErrorResource(String message) {
        this.message = message;
    }
}
