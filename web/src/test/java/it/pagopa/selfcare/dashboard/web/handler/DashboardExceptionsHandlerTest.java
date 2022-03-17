package it.pagopa.selfcare.dashboard.web.handler;

import it.pagopa.selfcare.commons.web.model.ErrorResource;
import it.pagopa.selfcare.dashboard.core.exception.FileValidationException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.dashboard.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DashboardExceptionsHandlerTest {

    private static final String DETAIL_MESSAGE = "detail message";

    private final DashboardExceptionsHandler handler;


    public DashboardExceptionsHandlerTest() {
        this.handler = new DashboardExceptionsHandler();
    }


    @Test
    void handleFileValidationException() {
        // given
        FileValidationException exceptionMock = Mockito.mock(FileValidationException.class);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ErrorResource resource = handler.handleFileValidationException(exceptionMock);
        // then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getMessage());
    }


    @Test
    void handleInvalidProductRoleException() {
        // given
        InvalidProductRoleException exceptionMock = Mockito.mock(InvalidProductRoleException.class);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ErrorResource resource = handler.handleInvalidProductRoleException(exceptionMock);
        // then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getMessage());
    }

    @Test
    void handleResourceNotFoundException() {
        //given
        ResourceNotFoundException exceptionMock = Mockito.mock(ResourceNotFoundException.class);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ErrorResource resource = handler.handleResourceNotFoundException(exceptionMock);
        //then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getMessage());
    }

}