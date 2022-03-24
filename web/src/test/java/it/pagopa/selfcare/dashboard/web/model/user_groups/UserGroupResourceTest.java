package it.pagopa.selfcare.dashboard.web.model.user_groups;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.web.model.ProductInfoResource;
import it.pagopa.selfcare.dashboard.web.model.ProductRoleInfoResource;
import it.pagopa.selfcare.dashboard.web.model.ProductUserResource;
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

class UserGroupResourceTest {
    private Validator validator;


    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void validateNullFields() {
        //given
        HashMap<String, Class<? extends Annotation>> toCheckMap = new HashMap<>();
        toCheckMap.put("id", NotBlank.class);
        toCheckMap.put("institutionId", NotBlank.class);
        toCheckMap.put("productId", NotBlank.class);
        toCheckMap.put("name", NotBlank.class);
        toCheckMap.put("description", NotBlank.class);
        toCheckMap.put("status", NotNull.class);
        UserGroupResource resource = new UserGroupResource();
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(resource);
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
        UserGroupResource resource = TestUtils.mockInstance(new UserGroupResource());
        ProductUserResource productUserResource = TestUtils.mockInstance(new ProductUserResource());
        ProductInfoResource productInfoResource = TestUtils.mockInstance(new ProductInfoResource());
        ProductRoleInfoResource productRoleInfoResource = TestUtils.mockInstance(new ProductRoleInfoResource());
        productInfoResource.setRoleInfos(List.of(productRoleInfoResource));
        productUserResource.setProduct(productInfoResource);
        resource.setMembers(List.of(productUserResource));
        PlainUserResource user = TestUtils.mockInstance(new PlainUserResource());
        resource.setModifiedBy(user);
        resource.setCreatedBy(user);
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(resource);
        // then
        assertTrue(violations.isEmpty());
    }
}