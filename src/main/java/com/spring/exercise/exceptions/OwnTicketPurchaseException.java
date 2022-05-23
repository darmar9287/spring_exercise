package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class OwnTicketPurchaseException extends CustomExceptions {

    public OwnTicketPurchaseException() {
        super(AppMessages.OWN_TICKET_PURCHASE_ERROR ,400);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(getMessage()));
    }
}