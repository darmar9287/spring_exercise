package com.spring.exercise.exceptions;

import com.spring.exercise.utils.ErrorPojo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public abstract class CustomExceptions extends RuntimeException {

    final int statusCode;
    String message;

    abstract public List<ErrorPojo> serializeErrors();

    public static ResponseEntity<?> handleCustomException(CustomExceptions ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("errors", ex.serializeErrors()));
    }

}
