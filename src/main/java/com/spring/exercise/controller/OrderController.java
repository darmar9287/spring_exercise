package com.spring.exercise.controller;

import com.spring.exercise.controller.model.order.OrderCreateRequest;
import com.spring.exercise.service.OrderServiceImpl;
import com.spring.exercise.utils.RequestBodyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/orders")
public class OrderController {

    private final OrderServiceImpl orderService;

    @PostMapping(produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> createTicketOrder(@Valid @RequestBody OrderCreateRequest orderCreateRequest,
                                               Errors errors,
                                               @RequestHeader(name = "Authorization") String token) {
        RequestBodyValidator.check(errors);
        var result = orderService.createTicketOrder(orderCreateRequest.getTicketId(), token);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping(produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getTicketOrdersForUser(
            @RequestHeader(name = "Authorization") String token) {
        var result = orderService.getTicketOrdersForUser(token);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping(value = "/{orderId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getTicketOrderForUser(@PathVariable String orderId,
                                                   @RequestHeader(name = "Authorization") String token) {
        var result = orderService.getTicketOrderForUser(token, orderId);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping(value = "/delete/{orderId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderId,
                                         @RequestHeader(name = "Authorization") String token) {
        orderService.deleteOrder(token, orderId);

        return ResponseEntity.noContent().build();
    }
}