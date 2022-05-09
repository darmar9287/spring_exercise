package com.spring.exercise.controller;

import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.controller.model.TicketDTO;
import com.spring.exercise.controller.model.TicketRequest;
import com.spring.exercise.controller.model.UserDTO;
import com.spring.exercise.exceptions.InvalidTicketCreateRequestException;
import com.spring.exercise.service.TicketServiceImpl;
import com.spring.exercise.utils.RequestBodyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/tickets")
public class TicketHandlingController {

    private final TicketServiceImpl ticketService;

    @PostMapping(value = "/create", produces = "application/json;charset=UTF-8")
    private ResponseEntity<?> createTicket(@Valid @RequestBody TicketRequest ticketRequest, @RequestHeader(name = "Authorization") String token, Errors errors) {
        RequestBodyValidator.check(errors);
        TicketDTO result = ticketService.createTicket(ticketRequest, token);

        final var response = ticketService.generateTicketCreateResponse(result, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
