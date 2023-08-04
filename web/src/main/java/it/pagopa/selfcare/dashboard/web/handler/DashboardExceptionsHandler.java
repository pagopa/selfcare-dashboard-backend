package it.pagopa.selfcare.dashboard.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.commons.web.model.mapper.ProblemMapper;
import it.pagopa.selfcare.dashboard.connector.exception.BadGatewayException;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.core.exception.FileValidationException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

/**
 * Exception handler for dashboard controllers
 */
@Slf4j
@RestControllerAdvice
public class DashboardExceptionsHandler {

    @ExceptionHandler({
            FileValidationException.class,
            InvalidProductRoleException.class
    })
    ResponseEntity<Problem> handleBadRequestException(Exception e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(BAD_REQUEST, e.getMessage()));
    }


    @ExceptionHandler({
            ResourceNotFoundException.class,
            InvalidUserGroupException.class
    })
    ResponseEntity<Problem> handleNotFoundException(Exception e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler({
            BadGatewayException.class,
    })
    ResponseEntity<Problem> handleBadGatewayException(Exception e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(BAD_GATEWAY, e.getMessage()));
    }

    @ExceptionHandler({
            RuntimeException.class,
    })
    ResponseEntity<Problem> handleRuntimeException(RuntimeException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(INTERNAL_SERVER_ERROR, e.getMessage()));
    }

}