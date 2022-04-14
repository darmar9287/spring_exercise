package com.spring.exercise.advice;

import com.spring.exercise.exceptions.CustomExceptions;
import com.spring.exercise.exceptions.UserAlreadyExists;
import com.spring.exercise.utils.ErrorPojo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler({CustomExceptions.class})
    public ResponseEntity<?> handleCustomException(CustomExceptions exception) {
        return CustomExceptions.handleCustomException(exception);
    }

}
