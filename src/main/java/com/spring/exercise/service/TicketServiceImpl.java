package com.spring.exercise.service;

import com.spring.exercise.controller.model.*;
import com.spring.exercise.exceptions.NotAuthorizedException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl {

    private final TicketRepository ticketRepository;
    private final JwtUtils jwtUtils;

    public TicketDTO createTicket(TicketRequest ticketRequest, String token) {
        String userId = jwtUtils.fetchUserIdFromToken(token);
        final var ticketEntity = new TicketEntity();
        ticketEntity.setTitle(ticketRequest.getTitle());
        ticketEntity.setPrice(ticketRequest.getPrice());
        ticketEntity.setUserId(userId);
        ticketRepository.save(ticketEntity);

        return TicketDTO.mapFromEntity(ticketEntity);
    }

    public TicketDTO updateTicket(TicketRequest ticketRequest, String ticketId, String token) {
        final var ticketEntity = ticketRepository.findById(ticketId);
        verifyIfTicketBelongsToUser(ticketId, token);
        final var fetchedTicket = ticketEntity.get();
        fetchedTicket.setTitle(ticketRequest.getTitle());
        fetchedTicket.setPrice(ticketRequest.getPrice());
        ticketRepository.save(fetchedTicket);

        return TicketDTO.mapFromEntity(fetchedTicket);
    }

    private void verifyIfTicketBelongsToUser(String ticketId, String token) {
        Optional<TicketEntity> ticket = ticketRepository.findById(ticketId);
        if(ticket.isEmpty()) {
            throw new NotFoundException(ticketId);
        }
        String userIdFromToken = jwtUtils.fetchUserIdFromToken(token);
        if(ticket.isPresent() && !ticket.get().getUserId().equals(userIdFromToken)) {
            throw new NotAuthorizedException();
        }
    }
}