package com.spring.exercise.unittests.service;

import com.spring.exercise.controller.model.TicketDTO;
import com.spring.exercise.controller.model.TicketListResponse;
import com.spring.exercise.controller.model.TicketRequest;
import com.spring.exercise.exceptions.NotAuthorizedException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.integrationtests.BaseIntegrationTests;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.service.TicketServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    private final static long FAKE_TICKET_VERSION = 1;
    private final static String FAKE_TOKEN = "fake_token";
    private final static String FAKE_USER_ID = "fake_user_id";
    private final static String FAKE_TICKET_TITLE = "fake_title";
    private final static BigDecimal FAKE_TICKET_PRICE = new BigDecimal(12);

    public TicketServiceTests() {
    }

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
    public void testCreateTicketShouldSuccessWhenRequestIsCorrect() {
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
    public void testShouldUpdateTicketSuccessWhenRequestIsCorrect() {
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
    public void testShouldNotPassVerifyWhenTicketIdIsNotCorrect() {
        //when
        when(ticketRepository.findById(any())).thenReturn(Optional.empty());
        //then
        assertThrows(NotFoundException.class, () -> ticketService.updateTicket(ticketRequest, ticketEntity.getId(), FAKE_TOKEN));
    }

    @Test
    public void testShouldNotPassVerifyWhenTicketDoesNotBelongToUser() {
        //given
        String wrongUserId = "wrong_id";
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(wrongUserId);
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticketEntity));
        //then
        assertThrows(NotAuthorizedException.class, () -> ticketService.updateTicket(ticketRequest, ticketEntity.getId(), FAKE_TOKEN));
    }

    @Test
    public void shouldReturnTicketsList() {
        //given
        List<TicketEntity> ticketsList = new ArrayList<>();
        for (int i = 0 ; i < 10 ; i++) {
            ticketsList.add(new TicketEntity(ObjectId.get().toString(),
                    FAKE_TICKET_VERSION,
                    FAKE_TICKET_TITLE + i,
                    FAKE_TICKET_PRICE,
                    ObjectId.get().toString()));
        }
        Page<TicketEntity> page = new PageImpl<>(ticketsList);
        //when
        PageRequest paging = PageRequest.of(0, 5);
        when(ticketRepository.findAll(paging)).thenReturn(page);
        //then
        TicketListResponse ticketResponse = ticketService.generateTicketListResponse(0, 5);
        assertEquals(10, ticketResponse.getTickets().size());
    }

    @Test
    public void findTicketByIdShouldReturnTicket() {
        //given
        String ticketId = ticketEntity.getId();
        //when
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticketEntity));
        //then
        TicketDTO foundTicket = ticketService.findTicketById(ticketId);
        assertEquals(foundTicket.getUserId(), ticketEntity.getUserId());
        assertEquals(foundTicket.getId(), ticketEntity.getId());
        assertEquals(foundTicket.getPrice(), ticketEntity.getPrice());
        assertEquals(foundTicket.getTitle(), ticketEntity.getTitle());
    }

    @Test
    public void findTicketByIdShouldThrowExceptionWhenTicketNotFound() {
        //given
        String fakeTicketId = "fake_ticket_id";
        //when
        when(ticketRepository.findById(fakeTicketId)).thenReturn(Optional.empty());
        //then
        assertThrows(NotFoundException.class, () -> ticketService.findTicketById(fakeTicketId));
    }
}