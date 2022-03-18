package it.pagopa.selfcare.dashboard.web.handler;

import it.pagopa.selfcare.commons.web.model.ErrorResource;
import it.pagopa.selfcare.dashboard.core.exception.FileValidationException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import it.pagopa.selfcare.dashboard.core.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler for dashboard controllers
 */
@Slf4j
@RestControllerAdvice
public class DashboardExceptionsHandler {

    @ExceptionHandler({FileValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResource handleFileValidationException(FileValidationException e) {
        log.warn(e.getMessage());
        return new ErrorResource(e.getMessage());
    }


    @ExceptionHandler({InvalidProductRoleException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResource handleInvalidProductRoleException(InvalidProductRoleException e) {
        log.warn(e.getMessage());
        return new ErrorResource(e.getMessage());
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResource handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn(e.getMessage());
        return new ErrorResource(e.getMessage());
    }

    @ExceptionHandler({InvalidUserGroupException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResource handleInvalidUSerGroupException(InvalidUserGroupException e) {
        log.warn(e.getMessage());
        return new ErrorResource(e.getMessage());
    }

}