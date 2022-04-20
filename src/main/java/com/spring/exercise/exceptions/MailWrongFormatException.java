package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppStringContainer;
import com.spring.exercise.utils.ErrorPojo;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.stream.Collectors;

public class MailWrongFormatException extends CustomExceptions {
    Errors errors;

    public MailWrongFormatException(Errors errors) {
        super(400, AppStringContainer.EMAIL_FORMAT_ERROR);
        this.errors = errors;
    }

    @Override
    public List<ErrorPojo> serializeErrors() {
        return errors.getFieldErrors().stream().map(x -> new ErrorPojo(x.getField(), x.getDefaultMessage())).collect(Collectors.toList());
    }
}
