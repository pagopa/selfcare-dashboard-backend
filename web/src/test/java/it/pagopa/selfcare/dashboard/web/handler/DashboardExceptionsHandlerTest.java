package it.pagopa.selfcare.dashboard.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.dashboard.connector.exception.InternalGatewayErrorException;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.core.exception.FileValidationException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidUserGroupException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.*;

class DashboardExceptionsHandlerTest {

    private static final String DETAIL_MESSAGE = "detail message";

    private final DashboardExceptionsHandler handler;


    public DashboardExceptionsHandlerTest() {
        this.handler = new DashboardExceptionsHandler();
    }


    @ParameterizedTest
    @ValueSource(classes = {
            FileValidationException.class,
            InvalidProductRoleException.class
    })
    void handleBadRequestException(Class<?> clazz) {
        // given
        Exception exceptionMock = (Exception) Mockito.mock(clazz);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ResponseEntity<Problem> responseEntity = handler.handleBadRequestException(exceptionMock);
        // then
        assertNotNull(responseEntity);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(BAD_REQUEST.value(), responseEntity.getBody().getStatus());
    }


    @ParameterizedTest
    @ValueSource(classes = {
            ResourceNotFoundException.class,
            InvalidUserGroupException.class
    })
    void handleNotFoundException(Class<?> clazz) {
        // given
        Exception exceptionMock = (Exception) Mockito.mock(clazz);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ResponseEntity<Problem> responseEntity = handler.handleNotFoundException(exceptionMock);
        // then
        assertNotNull(responseEntity);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(NOT_FOUND.value(), responseEntity.getBody().getStatus());
    }


    @ParameterizedTest
    @ValueSource(classes = {
            InternalGatewayErrorException.class
    })
    void handleInternalGatewayErrorException(Class<?> clazz) {
        // given
        InternalGatewayErrorException exceptionMock = (InternalGatewayErrorException) Mockito.mock(clazz);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ResponseEntity<Problem> responseEntity = handler.handleInternalGatewayErrorException(exceptionMock);
        // then
        assertNotNull(responseEntity);
        assertEquals(BAD_GATEWAY, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(BAD_GATEWAY.value(), responseEntity.getBody().getStatus());
    }

}