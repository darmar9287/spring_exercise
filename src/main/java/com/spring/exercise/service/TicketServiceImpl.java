package com.spring.exercise.service;

import com.spring.exercise.controller.model.*;
import com.spring.exercise.exceptions.InvalidCredentialsException;
import com.spring.exercise.exceptions.UserAlreadyExistsException;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.security.UserDetailsImpl;
import com.spring.exercise.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
}