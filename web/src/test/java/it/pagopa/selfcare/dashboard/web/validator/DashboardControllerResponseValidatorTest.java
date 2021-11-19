package it.pagopa.selfcare.dashboard.web.validator;

import it.pagopa.selfcare.commons.web.validator.ControllerResponseValidator;
import it.pagopa.selfcare.commons.web.validator.PointcutControllerResponseValidatorBaseTest;
import it.pagopa.selfcare.dashboard.web.controller.DummyController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootTest(classes = {
        ValidationAutoConfiguration.class,
        DummyController.class,
        DashboardControllerResponseValidator.class})
@EnableAspectJAutoProxy
class DashboardControllerResponseValidatorTest extends PointcutControllerResponseValidatorBaseTest {

    @Autowired
    private DummyController controller;

    @SpyBean
    private DashboardControllerResponseValidator validatorSpy;


    @Override
    protected ControllerResponseValidator getValidatorSpy() {
        return validatorSpy;
    }

    @Override
    protected void invokeNotVoidMethod() {
        controller.notVoidMethod();
    }

    @Override
    protected void invokeVoidMethod() {
        controller.voidMethod();
    }

}