package com.spring.exercise.exceptions;

import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class NotFoundException extends CustomExceptions {
    public NotFoundException(String message) {
        super(message, 404);
    }
    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(getMessage()));
    }
}