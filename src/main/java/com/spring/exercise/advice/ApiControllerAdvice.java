package com.spring.exercise.advice;

import com.spring.exercise.exceptions.CustomExceptions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
    public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler({CustomExceptions.class})
    public ResponseEntity<?> handleCustomException(CustomExceptions exception) {
        return CustomExceptions.handleCustomException(exception);
    }
}
