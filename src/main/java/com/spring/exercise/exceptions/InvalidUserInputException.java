package com.spring.exercise.exceptions;

import com.spring.exercise.utils.ErrorResponse;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.stream.Collectors;

public class InvalidUserInputException extends CustomExceptions {

    private Errors errors;

    public InvalidUserInputException(Errors errors) {
        super(400);
        this.errors = errors;
    }

    @Override
    public List<ErrorResponse> serializeErrors() {
        return errors.getFieldErrors().stream().
                map(x -> new ErrorResponse(x.getField(), x.getDefaultMessage())).collect(Collectors.toList());
    }
}
