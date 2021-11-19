package it.pagopa.selfcare.dashboard.web.validator;

import it.pagopa.selfcare.dashboard.core.ProductsService;
import it.pagopa.selfcare.dashboard.web.controller.DummyController;
import it.pagopa.selfcare.dashboard.web.controller.ProductsController;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest(classes = {DummyController.class, DashboardControllerResponseValidator.class})
@EnableAutoConfiguration
class DashboardControllerResponseValidatorTest {

    @SpyBean
    private DashboardControllerResponseValidator validatorSpy;

    @Autowired
    private DummyController controller;

    @Test
    void controllersPointcut_returnNotVoid() {
        // given
        // when
        controller.notVoidMethod();
        // then
        Mockito.verify(validatorSpy, Mockito.times(1))
                .validateResponse(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(validatorSpy);
    }

    @Test
    void controllersPointcut_returnVoid() {
        // given
        // when
        controller.voidMethod();
        // then
        Mockito.verify(validatorSpy, Mockito.times(1))
                .validateResponse(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(validatorSpy);
    }

    @Test
    void controllersPointcut() {//sonar fix
        validatorSpy.controllersPointcut();
    }
}