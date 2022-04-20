package com.spring.exercise.advice;

import com.spring.exercise.exceptions.CustomExceptions;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler({CustomExceptions.class})
    public ResponseEntity<?> handleCustomException(CustomExceptions exception) {
        return CustomExceptions.handleCustomException(exception);
    }
//
//    @ExceptionHandler({MethodArgumentNotValidException.class})
//    public ResponseEntity<?> handleMethodArgumentException(HttpServletResponse response) {
//        response
//        return null;
//    }






}
