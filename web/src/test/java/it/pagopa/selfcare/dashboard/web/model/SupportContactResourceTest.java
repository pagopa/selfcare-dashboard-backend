package it.pagopa.selfcare.dashboard.web.model;

import it.pagopa.selfcare.commons.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SupportContactResourceTest {

    private Validator validator;
    private static final SupportContactResource SUPPORT_CONTACT_RESOURCE = TestUtils.mockInstance(new SupportContactResource());

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }


    @Test
    void validateNotNullFields() {
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(SUPPORT_CONTACT_RESOURCE);
        // then
        assertTrue(violations.isEmpty());
    }

}