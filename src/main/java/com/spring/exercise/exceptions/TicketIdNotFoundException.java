package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class TicketIdNotFoundException extends CustomExceptions{

    public TicketIdNotFoundException(String ticketId) {
        super(AppMessages.TICKET_NOT_FOUND_ERROR + ticketId, 404);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(getMessage()));
    }
}