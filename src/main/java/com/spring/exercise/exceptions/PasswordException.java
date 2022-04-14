package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppStringContainer;
import com.spring.exercise.utils.ErrorPojo;
import lombok.Getter;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
