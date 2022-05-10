package com.spring.exercise.unittests.service;

import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.controller.model.TicketDTO;
import com.spring.exercise.controller.model.TicketRequest;
import com.spring.exercise.controller.model.UserDTO;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.service.TicketServiceImpl;
import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import org.bson.types.ObjectId;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTests {

    @InjectMocks
    private TicketServiceImpl ticketService;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private JwtUtils jwtUtils;

    private TicketRequest ticketRequest;
    private TicketEntity ticketEntity;

    private final static String FAKE_TOKEN = "fake_token";
    private final static String FAKE_USER_ID = "fake_user_id";
    private final static String FAKE_TICKET_TITLE = "fake_title";
    private final static BigDecimal FAKE_TICKET_PRICE = new BigDecimal(12);



    @BeforeEach
    public void setUp() {
        ticketRequest = new TicketRequest();
        ticketRequest.setTitle("ticket_title");
        ticketRequest.setPrice(new BigDecimal(13));

        ticketEntity = new TicketEntity();
        ticketEntity.setUserId(FAKE_USER_ID);
        ticketEntity.setTitle(FAKE_TICKET_TITLE);
        ticketEntity.setPrice(FAKE_TICKET_PRICE);
    }

    @Test
    void testCreateTicketShouldSuccessWhenRequestIsCorrect() {
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(ticketRepository.save(ticketEntity)).thenReturn(ticketEntity);
        //then
        TicketDTO createdTicket = ticketService.createTicket(ticketRequest, FAKE_TOKEN);
        assertEquals(createdTicket.getTitle(), ticketEntity.getTitle());
    }


}
