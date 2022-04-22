package com.spring.exercise.exceptions;

import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;
import org.springframework.validation.Errors;

import java.util.List;

public class UserAlreadyExists extends CustomExceptions {

    private final static String error = AppMessages.EMAIL_EXISTS_ERROR;

    public UserAlreadyExists(Errors errors) {
        super(401, errors);
    }


    @Override
    public List<ErrorResponse> serializeErrors() {
        return List.of(new ErrorResponse(error));
    }
}
