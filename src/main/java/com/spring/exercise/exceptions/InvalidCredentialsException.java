package com.spring.exercise.exceptions;

import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class InvalidCredentialsException extends CustomExceptions {

    String message;

    public InvalidCredentialsException(String message) {
       super(401, null);
       this.message = message;
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(message));
    }
}
