package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class InvalidCredentialsException extends CustomExceptions {

    public InvalidCredentialsException() {
       super(AppMessages.INCORRECT_CREDENTIALS_ERROR, 401);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(getMessage()));
    }
}
