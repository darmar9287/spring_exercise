package com.spring.exercise.service;

import com.spring.exercise.model.ticket.TicketDTO;
import com.spring.exercise.model.ticket.TicketListResponse;
import com.spring.exercise.model.ticket.TicketRequest;
import com.spring.exercise.model.ticket.TicketResponse;
import com.spring.exercise.exceptions.BadRequestException;
import com.spring.exercise.exceptions.NotAuthorizedException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.entity.TicketEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl {

    private static final int MAX_TICKET_SIZE = 50;
    private final TicketRepository ticketRepository;
    private final JwtUtils jwtUtils;

    public TicketDTO createTicket(TicketRequest ticketRequest, String token) {
        String userId = jwtUtils.fetchUserIdFromToken(token);
        final var ticketEntity = new TicketEntity();
        ticketEntity.setTitle(ticketRequest.getTitle());
        ticketEntity.setPrice(ticketRequest.getPrice());
        ticketEntity.setDescription(ticketRequest.getDescription());
        ticketEntity.setUserId(userId);
        ticketRepository.save(ticketEntity);
        return TicketDTO.mapFromEntity(ticketEntity);
    }

    public TicketDTO updateTicket(TicketRequest ticketRequest, String ticketId, String token) {
        final var ticketEntity = ticketRepository.findById(ticketId);
        if(ticketEntity.isEmpty()) {
            log.warn("Did not find ticket with id " + ticketId);
            throw new NotFoundException("Not found ticket with id " + ticketId);
        }
        var foundTicket = ticketEntity.get();
        if (foundTicket.getOrderId() != null) {
            log.warn("Ticket with id " + ticketId + " is already booked");
            throw new BadRequestException("Ticket with id " + ticketId + " is already booked");
        }
        verifyIfTicketBelongsToUser(foundTicket, token);
        foundTicket.setTitle(ticketRequest.getTitle());
        foundTicket.setPrice(ticketRequest.getPrice());
        ticketRepository.save(foundTicket);

        return TicketDTO.mapFromEntity(foundTicket);
    }

    public TicketListResponse generateTicketListResponse(int currentPage, int size) {
        Pageable paging = PageRequest.of(currentPage, size);
        if(size > MAX_TICKET_SIZE) {
            paging = PageRequest.of(currentPage, MAX_TICKET_SIZE);
        }

        Page<TicketEntity> pageTickets = ticketRepository.findAll(paging);
        List<TicketResponse> tickets = pageTickets.getContent().stream().map(ticket -> TicketResponse.builder()
                .id(ticket.getId())
                .price(ticket.getPrice())
                .title(ticket.getTitle())
                .userId(ticket.getUserId())
                .build()).collect(Collectors.toList());

        return TicketListResponse.builder().tickets(tickets).
                currentPage(currentPage).
                totalTickets(pageTickets.getTotalElements()).
                totalPages(pageTickets.getTotalPages()).
                build();
    }

    public TicketDTO findTicketById(String id) {
        Optional<TicketEntity> ticketEntity = ticketRepository.findById(id);
        String errorMessage = "not found ticket with id " + id;
        if (ticketEntity.isEmpty()) {
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return TicketDTO.mapFromEntity(ticketEntity.get());
    }

    private void verifyIfTicketBelongsToUser(TicketEntity ticket, String token) {
        String userIdFromToken = jwtUtils.fetchUserIdFromToken(token);
        if(!ticket.getUserId().equals(userIdFromToken)) {
            log.warn("User is not authorized");
            throw new NotAuthorizedException();
        }
    }
}