package com.spring.exercise.controller;

import com.spring.exercise.controller.model.TicketCreateResponse;
import com.spring.exercise.controller.model.TicketDTO;
import com.spring.exercise.controller.model.TicketRequest;
import com.spring.exercise.exceptions.TicketIdNotFoundException;
import com.spring.exercise.service.TicketServiceImpl;
import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.ErrorResponse;
import com.spring.exercise.utils.JwtUtils;
import com.spring.exercise.utils.RequestBodyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/tickets")
public class TicketHandlingController {

    private final JwtUtils jwtUtils;
    private final TicketServiceImpl ticketService;


    @PostMapping(value = "/create", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> createTicket(@Valid @RequestBody TicketRequest ticketRequest,
                                          Errors errors,
                                          @RequestHeader(name = "Authorization") String token) {
        RequestBodyValidator.check(errors);
        TicketDTO result = ticketService.createTicket(ticketRequest, token);
        final var response = TicketCreateResponse.mapFromDTO(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/update", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> updateTicket(@Valid @RequestBody TicketRequest ticketRequest,
                                          Errors errors,
                                          @RequestParam String ticketId,
                                          @RequestHeader(name = "Authorization") String token) {
        RequestBodyValidator.check(errors);
        TicketDTO result = ticketService.updateTicket(ticketRequest, ticketId, token);
        final var response = TicketCreateResponse.mapFromDTO(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}