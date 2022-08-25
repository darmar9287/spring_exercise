package com.spring.exercise.utils.CustomValidators;

import com.spring.exercise.utils.AppMessages;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = AgeValidator.class)
@Documented
public @interface Age {
    String message() default AppMessages.AGE_REQUIREMENT_ERROR;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

