package it.pagopa.selfcare.dashboard.web.model;

import it.pagopa.selfcare.commons.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateUserDtoTest {

    private Validator validator;
    private static final UpdateUserDto USER_DTO = TestUtils.mockInstance(new UpdateUserDto());

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void validateNullFields() {
        //given
        HashMap<String, Class<? extends Annotation>> toCheckMap = new HashMap<>();
        toCheckMap.put("name", NotBlank.class);
        toCheckMap.put("surname", NotBlank.class);
        toCheckMap.put("email", NotBlank.class);

        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setEmail(null);
        userDto.setName(null);
        userDto.setSurname(null);
        //when
        Set<ConstraintViolation<Object>> violations = validator.validate(userDto);
        //then
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
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(USER_DTO);
        // then
        assertTrue(violations.isEmpty());
    }

}