package it.pagopa.selfcare.dashboard.web.validator;

import it.pagopa.selfcare.dashboard.core.ProductsService;
import it.pagopa.selfcare.dashboard.web.controller.ProductsController;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest(classes = {ProductsController.class, DashboardControllerResponseValidator.class})
@EnableAutoConfiguration
class DashboardControllerResponseValidatorTest {

    @SpyBean
    private DashboardControllerResponseValidator validatorSpy;

    @Autowired
    private ProductsController controller;

    @MockBean
    private ProductsService productsServiceMock;

    @Test
    void controllersPointcut_returnNotVoid() {
        // given
        // when
        controller.getProducts();
        // then
        Mockito.verify(validatorSpy, Mockito.times(1))
                .validateResponse(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(validatorSpy);
    }

    @Test
    void controllersPointcut_returnVoid() {
        // given
        // when
        controller.getProducts();
        // then
        Mockito.verify(validatorSpy, Mockito.times(1))
                .validateResponse(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(validatorSpy);
    }
}