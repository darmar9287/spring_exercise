package com.spring.exercise.exceptions;

import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class BadRequestException extends CustomExceptions {

    public BadRequestException(String message) {
        super(message, 400);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(getMessage()));
    }

}