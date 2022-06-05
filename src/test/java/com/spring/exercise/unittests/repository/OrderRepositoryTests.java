package com.spring.exercise.unittests.repository;

import com.spring.exercise.entity.OrderEntity;
import com.spring.exercise.entity.TicketEntity;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.utils.OrderStatus;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;

@DataMongoTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/test.properties")
public class OrderRepositoryTests {
    @Autowired
    private OrderRepository orderRepository;

    private final static String TICKET_TITLE = "fake_title";
    private final static BigDecimal TICKET_PRICE = new BigDecimal(13);
    private final static String USER_ID = "1";
    private final static TicketEntity ticket = new TicketEntity();

    @AfterEach
    public void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    public void shouldReturnOrdersListForUser() {
        //given
        List<OrderEntity> orders = new ArrayList<>();
        List<TicketEntity> tickets = new ArrayList<>();
        IntStream.range(0, 10)
                .forEach(x -> {
                    tickets.add(new TicketEntity(ObjectId.get().toString(),
                            TICKET_TITLE,
                            TICKET_PRICE,
                            USER_ID,
                            ObjectId.get().toString()));
                });
        IntStream.range(0, 10)
                .forEach(x -> {
                    orders.add(OrderEntity.builder()
                            .id(ObjectId.get().toString())
                            .userId(USER_ID)
                            .orderStatus(OrderStatus.CREATED)
                            .expiresAt(LocalDateTime.now())
                            .ticket(tickets.get(x)).build());
                });
        //when
        orderRepository.saveAll(orders);
        //then
        List<OrderEntity> ordersFromDb = StreamSupport.stream(orderRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        assertEquals(orders.size(), ordersFromDb.size());
    }
}