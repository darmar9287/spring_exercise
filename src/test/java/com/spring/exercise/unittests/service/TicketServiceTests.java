package com.spring.exercise.unittests.service;

import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.controller.model.TicketDTO;
import com.spring.exercise.controller.model.TicketRequest;
import com.spring.exercise.controller.model.UserDTO;
import com.spring.exercise.exceptions.TicketDoesNotBelongToUserException;
import com.spring.exercise.exceptions.TicketIdNotFoundException;
import com.spring.exercise.integrationtests.BaseIntegrationTests;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.service.TicketServiceImpl;
import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTests extends BaseIntegrationTests {

    @InjectMocks
    private TicketServiceImpl ticketService;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private JwtUtils jwtUtils;

    private TicketRequest ticketRequest;
    private TicketEntity ticketEntity;

    private final static String FAKE_TICKET_ID = "fake_ticket_id";
    private final static String FAKE_TOKEN = "fake_token";
    private final static String FAKE_USER_ID = "fake_user_id";
    private final static String FAKE_TICKET_TITLE = "fake_title";
    private final static BigDecimal FAKE_TICKET_PRICE = new BigDecimal(12);

    public TicketServiceTests() {}

    @BeforeEach
    public void setUp() {
        ticketRequest = new TicketRequest();
        ticketRequest.setTitle(FAKE_TICKET_TITLE);
        ticketRequest.setPrice(FAKE_TICKET_PRICE);

        ticketEntity = new TicketEntity();
        ticketEntity.setId(ObjectId.get().toString());
        ticketEntity.setUserId(FAKE_USER_ID);
        ticketEntity.setTitle(FAKE_TICKET_TITLE);
        ticketEntity.setPrice(FAKE_TICKET_PRICE);
    }

    @Test
    void testCreateTicketShouldSuccessWhenRequestIsCorrect() {
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(ticketRepository.save(any())).thenReturn(ticketEntity);
        //then
        TicketDTO createdTicket = ticketService.createTicket(ticketRequest, FAKE_TOKEN);
        assertEquals(createdTicket.getTitle(), ticketEntity.getTitle());
        assertEquals(createdTicket.getPrice(), ticketEntity.getPrice());
        assertEquals(createdTicket.getUserId(), ticketEntity.getUserId());
    }

    @Test
    void testShouldUpdateTicketSuccessWhenRequestIsCorrect() {
        //when
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticketEntity));
        when(ticketRepository.save(any())).thenReturn(ticketEntity);
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        //then
        TicketDTO updatedTicket = ticketService.updateTicket(ticketRequest, ticketEntity.getId(), FAKE_TOKEN);
        assertEquals(updatedTicket.getTitle(), ticketEntity.getTitle());
        assertEquals(updatedTicket.getPrice(), ticketEntity.getPrice());
    }

    @Test
    void testShouldNotPassVerifyWhenTicketIdIsNotCorrect() {
        //when
        when(ticketRepository.findById(any())).thenReturn(Optional.empty());
        //then
        assertThrows(TicketIdNotFoundException.class, () -> ticketService.updateTicket(ticketRequest, ticketEntity.getId(), FAKE_TOKEN));
    }

    @Test
    void testShouldNotPassVerifyWhenTicketDoesNotBelongToUser() {
        //given
        String wrongUserId = "wrong_id";
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(wrongUserId);
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticketEntity));
        //then
        assertThrows(TicketDoesNotBelongToUserException.class, () -> ticketService.updateTicket(ticketRequest, ticketEntity.getId(), FAKE_TOKEN));
    }
}