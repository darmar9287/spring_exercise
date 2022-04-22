package com.spring.exercise.advice;

import com.spring.exercise.exceptions.CustomExceptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler( {CustomExceptions.class} )
    public ResponseEntity<?> handleCustomException(CustomExceptions exception) {
        return CustomExceptions.handleCustomException(exception);
    }
}
