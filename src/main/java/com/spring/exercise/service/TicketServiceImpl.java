package com.spring.exercise.service;

import com.spring.exercise.controller.model.*;
import com.spring.exercise.exceptions.NotAuthorizedException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl {

    private static final int MAX_TICKET_SIZE = 50;
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

    public TicketListResponse generateTicketListResponse(int currentPage, int size) {
        Pageable paging = PageRequest.of(currentPage, size);
        if(size > MAX_TICKET_SIZE) {
            paging = PageRequest.of(currentPage, MAX_TICKET_SIZE);
        }

        Page<TicketEntity> pageTickets = ticketRepository.findAll(paging);
        List<TicketResponse> tickets = pageTickets.getContent().stream().map(x -> TicketResponse.mapFromEntity(x)).collect(Collectors.toList());

        return TicketListResponse.builder().tickets(tickets).
                currentPage(currentPage).
                totalTickets(pageTickets.getTotalElements()).
                totalPages(pageTickets.getTotalPages()).
                build();
    }

    public TicketDTO findTicketById(String id) {
        Optional<TicketEntity> ticketEntity = ticketRepository.findById(id);
        if(ticketEntity.isEmpty()) {
            throw new NotFoundException(id);
        }
        return TicketDTO.mapFromEntity(ticketEntity.get());
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