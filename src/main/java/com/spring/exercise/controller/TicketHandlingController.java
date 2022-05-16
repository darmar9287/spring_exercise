package com.spring.exercise.controller;

import com.spring.exercise.controller.model.TicketResponse;
import com.spring.exercise.controller.model.TicketDTO;
import com.spring.exercise.controller.model.TicketListResponse;
import com.spring.exercise.controller.model.TicketRequest;
import com.spring.exercise.service.TicketServiceImpl;
import com.spring.exercise.utils.RequestBodyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/tickets")
public class TicketHandlingController {

    private final TicketServiceImpl ticketService;

    @PostMapping(value = "/create", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> createTicket(@Valid @RequestBody TicketRequest ticketRequest,
                                          Errors errors,
                                          @RequestHeader(name = "Authorization") String token) {
        RequestBodyValidator.check(errors);
        TicketDTO result = ticketService.createTicket(ticketRequest, token);
        final var response = TicketResponse.mapFromDTO(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/update/{ticketId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> updateTicket(@Valid @RequestBody TicketRequest ticketRequest,
                                          Errors errors,
                                          @PathVariable String ticketId,
                                          @RequestHeader(name = "Authorization") String token) {
        RequestBodyValidator.check(errors);
        TicketDTO result = ticketService.updateTicket(ticketRequest, ticketId, token);
        final var response = TicketResponse.mapFromDTO(result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllTicketsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") @Max(10) int size) {
            TicketListResponse ticketCreateResponse = ticketService.generateTicketListResponse(page, size);
            return ResponseEntity.status(HttpStatus.OK).body(ticketCreateResponse);
    }

    @GetMapping(value = "/show/{ticketId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> showTicket(@PathVariable String ticketId) {
        TicketDTO result = ticketService.findTicketById(ticketId);
        final var response = TicketResponse.mapFromDTO(result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}