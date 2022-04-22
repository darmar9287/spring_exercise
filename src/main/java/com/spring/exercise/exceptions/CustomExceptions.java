package com.spring.exercise.exceptions;

import com.spring.exercise.utils.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public abstract class CustomExceptions extends RuntimeException {

    int statusCode;
    Errors errors;

    abstract public List<ErrorResponse> serializeErrors();

    public static ResponseEntity<?> handleCustomException(CustomExceptions ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("errors", ex.serializeErrors()));
    }


}
