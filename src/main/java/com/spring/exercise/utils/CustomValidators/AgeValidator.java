package com.spring.exercise.utils.CustomValidators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<Age, LocalDate> {

    private static int MINIMUM_REQUIRED_AGE;

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext constraintValidatorContext) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears() > MINIMUM_REQUIRED_AGE;
    }
}
