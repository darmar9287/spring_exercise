package com.spring.exercise.controller;


import com.spring.exercise.model.payment.PaymentRequest;
import com.spring.exercise.service.PaymentServiceImpl;
import com.spring.exercise.utils.RequestBodyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/payments")
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    @PostMapping(value = "/create", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentRequest paymentRequest,
                                          Errors errors,
                                          @RequestHeader(name = "Authorization") String token) {
        RequestBodyValidator.check(errors);
        var result = paymentService.createPayment(paymentRequest, token);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}