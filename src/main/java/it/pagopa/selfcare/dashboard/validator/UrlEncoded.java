package it.pagopa.selfcare.dashboard.validator;

import it.pagopa.selfcare.dashboard.utils.EncodingUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UrlEncoded.UrlEncodedConstraintValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlEncoded {
    String message() default "The string must be URL-encoded";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class UrlEncodedConstraintValidator implements ConstraintValidator<UrlEncoded, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
            return EncodingUtils.isUrlEncoded(value);
        }

    }

}
