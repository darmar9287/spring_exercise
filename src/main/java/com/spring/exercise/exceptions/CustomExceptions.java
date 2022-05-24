package com.spring.exercise.exceptions;

import com.spring.exercise.utils.ErrorResponse;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Getter
public abstract class CustomExceptions extends RuntimeException {

    private int statusCode;

    public CustomExceptions(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CustomExceptions(int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    abstract public List<ErrorResponse> serializeErrors();

    public static ResponseEntity<?> handleCustomException(CustomExceptions ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("errors", ex.serializeErrors()));
    }
}