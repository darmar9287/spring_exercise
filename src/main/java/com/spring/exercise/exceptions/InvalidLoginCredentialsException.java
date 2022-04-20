package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppStringContainer;
import com.spring.exercise.utils.ErrorPojo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InvalidLoginCredentialsException extends CustomExceptions {

    public InvalidLoginCredentialsException() {
        super(401, AppStringContainer.INCORRECT_CREDENTIALS_ERROR);
    }

    @Override
    public List<ErrorPojo> serializeErrors() {
        return Arrays.asList("Invalid login credentials").stream().map(x -> new ErrorPojo(x)).collect(Collectors.toList());
    }
}
