package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class OrderNotFoundException extends CustomExceptions {

    public OrderNotFoundException(String orderId) {
        super("Order with id " + orderId + " was not found", 404);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(getMessage()));
    }
}