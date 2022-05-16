package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class TicketListSizeTooBigException extends CustomExceptions{
    public TicketListSizeTooBigException(String ticketId) {
        super(AppMessages.TICKET_SIZE_TOO_BIG_ERROR + ticketId, 404);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(getMessage()));
    }
}
