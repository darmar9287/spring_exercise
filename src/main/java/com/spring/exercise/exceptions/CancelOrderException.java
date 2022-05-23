package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class CancelOrderException extends CustomExceptions {

    public CancelOrderException(String orderId) {
        super("Cannot cancel order with id: " + orderId + ". Status is COMPLETED/CANCELLED" ,400);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(getMessage()));
    }
}