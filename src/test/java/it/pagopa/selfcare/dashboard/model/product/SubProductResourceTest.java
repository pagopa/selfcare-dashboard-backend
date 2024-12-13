package it.pagopa.selfcare.dashboard.model.product;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.model.product.SubProductResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SubProductResourceTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void validateNullFields() {
        // given
        HashMap<String, Class<? extends Annotation>> toCheckMap = new HashMap<>();
        toCheckMap.put("id", NotBlank.class);
        toCheckMap.put("title", NotBlank.class);
        toCheckMap.put("productOnBoardingStatus", NotNull.class);
        toCheckMap.put("status", NotNull.class);
        SubProductResource subProductResource = new SubProductResource();
        subProductResource.setId(null);
        subProductResource.setTitle(null);
        subProductResource.setProductOnBoardingStatus(null);
        subProductResource.setStatus(null);
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(subProductResource);
        // then
        List<ConstraintViolation<Object>> filteredViolations = violations.stream()
                .filter(violation -> {
                    Class<? extends Annotation> annotationToCheck = toCheckMap.get(violation.getPropertyPath().toString());
                    return !violation.getConstraintDescriptor().getAnnotation().annotationType().equals(annotationToCheck);
                })
                .collect(Collectors.toList());
        assertTrue(filteredViolations.isEmpty());
    }

    @Test
    void validateNotNullFields() {
        // given
        SubProductResource subProductResource = TestUtils.mockInstance(new SubProductResource());
        subProductResource.setLogoBgColor("#FF56E1");
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(subProductResource);
        // then
        assertTrue(violations.isEmpty());
    }
}
