package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;

import java.util.List;

public class TicketAlreadyReservedException extends CustomExceptions {

    private final static String error = AppMessages.TICKET_ALREADY_BOOKED_ERROR;

    public TicketAlreadyReservedException() {
        super(400);
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(error));
    }
}
