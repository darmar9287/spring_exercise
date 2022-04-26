package com.spring.exercise.utils;

import com.spring.exercise.exceptions.InvalidUserInputException;
import org.springframework.validation.Errors;

public class RequestBodyValidator {

    public RequestBodyValidator() { }

    public static void check(Errors errors) {
        if (errors.hasErrors()) {
            throw new InvalidUserInputException(errors);
        }
    }
}
