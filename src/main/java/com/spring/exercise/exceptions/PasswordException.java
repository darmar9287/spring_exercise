package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppStringContainer;
import com.spring.exercise.utils.ErrorPojo;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.stream.Collectors;

public class PasswordException extends CustomExceptions {
    Errors errors;

    public PasswordException(Errors errors) {
        super(400, AppStringContainer.PASSWORD_SIZE_ERROR);
        this.errors = errors;
    }

    @Override
    public List<ErrorPojo> serializeErrors() {
        return errors.getFieldErrors().stream().map(x -> new ErrorPojo(x.getField(), x.getDefaultMessage())).collect(Collectors.toList());
    }
}