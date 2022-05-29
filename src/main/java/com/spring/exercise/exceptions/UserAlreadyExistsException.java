package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class UserAlreadyExistsException extends CustomExceptions {

    private final static String error = AppMessages.EMAIL_EXISTS_ERROR;

    public UserAlreadyExistsException() {
        super(401);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(error));
    }
}