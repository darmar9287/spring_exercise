package com.spring.exercise.utils;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueMailValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueMail {
    String message() default AppStringContainer.EMAIL_EXISTS_ERROR;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}