package com.spring.exercise.utils.CustomValidators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<Age, LocalDate> {

    private int minAge;

    @Override
    public void initialize(final Age constraintAnnotation) {
        minAge = constraintAnnotation.value()[0];
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext constraintValidatorContext) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears() > minAge;
    }
}
