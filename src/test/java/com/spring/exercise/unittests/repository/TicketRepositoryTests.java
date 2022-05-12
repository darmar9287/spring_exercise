package com.spring.exercise.unittests.repository;

import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@DataMongoTest
@RunWith(SpringRunner.class)
public class TicketRepositoryTests {
    @Autowired
    private TicketRepository ticketRepository;

    private TicketEntity ticket;
    private final static String TICKET_TITLE = "fake_title";
    private final static BigDecimal TICKET_PRICE = new BigDecimal(13);
    private final static String USER_ID = "user_id";

    @BeforeEach
    public void dataSetup() {
        ticket = new TicketEntity();
        ticket.setId(ObjectId.get().toString());
        ticket.setTitle(TICKET_TITLE);
        ticket.setPrice(TICKET_PRICE);
        ticket.setUserId(USER_ID);
    }

    @AfterEach
    public void tearDown() {
        ticketRepository.deleteAll();
    }

    @Test
    public void shouldSaveTicket() {
        //when
        TicketEntity savedTicket = ticketRepository.save(ticket);
        //then
        TicketEntity fetchedTicket = ticketRepository.findById(ticket.getId()).get();
        assertEquals(savedTicket.getTitle(), fetchedTicket.getTitle());
        assertEquals(savedTicket.getPrice(), fetchedTicket.getPrice());
        assertEquals(savedTicket.getUserId(), fetchedTicket.getUserId());
    }
}
