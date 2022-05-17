package com.spring.exercise.unittests.repository;

import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.repository.TicketRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        ticket = new TicketEntity(ObjectId.get().toString(), 0L, TICKET_TITLE, TICKET_PRICE, USER_ID);
//        ticket.setId(ObjectId.get().toString());
//        ticket.setTitle(TICKET_TITLE);
//        ticket.setPrice(TICKET_PRICE);
//        ticket.setUserId(USER_ID);
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

    @Test
    public void shouldReturnTicketsList() {
        //given
        List<TicketEntity> ticketsList = new ArrayList<>();
        for (int i = 0 ; i < 10 ; i++) {
            ticketsList.add(new TicketEntity(ObjectId.get().toString(),
                    2L,
                    TICKET_TITLE + i,
                    TICKET_PRICE,
                    ObjectId.get().toString()));
        }
        //when
        ticketRepository.saveAll(ticketsList);
        //then
        List<TicketEntity> ticketsFromDb = StreamSupport.stream(ticketRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        assertEquals(ticketsList.size(), ticketsFromDb.size());
    }
}