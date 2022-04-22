package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;
import org.springframework.validation.Errors;

import java.util.List;

public class UserAlreadyExistsException extends CustomExceptions {

    private final static String error = AppMessages.EMAIL_EXISTS_ERROR;

    public UserAlreadyExistsException(Errors errors) {
        super(401);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(error));
    }
}
