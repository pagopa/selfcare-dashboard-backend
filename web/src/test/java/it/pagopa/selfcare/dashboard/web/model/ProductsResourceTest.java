package it.pagopa.selfcare.dashboard.web.model;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.web.model.product.ProductsResource;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductsResourceTest {

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
        toCheckMap.put("logo", NotBlank.class);
        toCheckMap.put("imageUrl", NotBlank.class);
        toCheckMap.put("title", NotBlank.class);
        toCheckMap.put("description", NotBlank.class);
        toCheckMap.put("urlBO", NotBlank.class);
        toCheckMap.put("status", NotNull.class);
        ProductsResource productsResource = new ProductsResource();
        productsResource.setId(null);
        productsResource.setLogo(null);
        productsResource.setImageUrl(null);
        productsResource.setTitle(null);
        productsResource.setDescription(null);
        productsResource.setLogoBgColor(null);
        productsResource.setUrlBO(null);
        productsResource.setStatus(null);
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(productsResource);
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
    void validateRegExViolation() {
        //given
        ProductsResource productsResource = TestUtils.mockInstance(new ProductsResource());
        //when
        Set<ConstraintViolation<Object>> violations = validator.validate(productsResource);
        // then
        assertFalse(violations.isEmpty());
    }

    @Test
    void validateNotNullFields() {
        // given
        ProductsResource productsResource = TestUtils.mockInstance(new ProductsResource());
        productsResource.setLogoBgColor("#FF56E1");
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(productsResource);
        // then
        assertTrue(violations.isEmpty());
    }

}