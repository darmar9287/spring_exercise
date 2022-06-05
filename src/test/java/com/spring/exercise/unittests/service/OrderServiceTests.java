package com.spring.exercise.unittests.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.spring.exercise.configuration.SQSConnectionConfiguration;
import com.spring.exercise.model.order.OrderResponse;
import com.spring.exercise.exceptions.BadRequestException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.integrationtests.BaseIntegrationTests;
import com.spring.exercise.entity.OrderEntity;
import com.spring.exercise.entity.TicketEntity;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.service.OrderServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import com.spring.exercise.utils.OrderStatus;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests extends BaseIntegrationTests {

    @InjectMocks
    private OrderServiceImpl orderService;
    @Mock
    private SQSConnectionConfiguration sqsConnectionConfiguration;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AmazonSQSAsync sqsClient;

    private TicketEntity ticket;
    private OrderEntity order;

    private final static String FAKE_TOKEN = "fake_token";
    private final static String FAKE_TICKET_ID = "fake_ticket_id";
    private final static String FAKE_ORDER_ID = "fake_order_id";
    private final static String FAKE_USER_ID = "fake_user_id";
    private final static String FAKE_USER_ID_2 = "fake_user_id_2";
    private final static String FAKE_TICKET_TITLE = "fake_title";
    private final static BigDecimal FAKE_TICKET_PRICE = new BigDecimal(12);

    public OrderServiceTests() {
    }

    @BeforeEach
    public void setUp() {
        ticket = new TicketEntity();
        ticket.setId(FAKE_TICKET_ID);
        ticket.setTitle(FAKE_TICKET_TITLE);
        ticket.setPrice(FAKE_TICKET_PRICE);
        ticket.setUserId(ObjectId.get().toString());
        ticket.setOrderId(null);
        order = new OrderEntity(FAKE_ORDER_ID,
                ObjectId.get().toString(),
                OrderStatus.CREATED,
                LocalDateTime.now(),
                ticket);
    }

    @Test
    public void testCreateOrderShouldSuccessWhenRequestIsCorrect() {
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID_2);
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticket));
        when(sqsConnectionConfiguration.amazonSQSClient()).thenReturn(sqsClient);
        when(sqsClient.getQueueUrl("order-expiration")).thenReturn(new GetQueueUrlResult());
        //then
        OrderResponse orderResponse = orderService.createTicketOrder(FAKE_TICKET_ID, FAKE_TOKEN);
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
        assertEquals(orderResponse.getTicket().getTicketId(), order.getTicket().getId());
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
    }

    @Test
    public void testShouldThrowExceptionWhenTicketNotFound() {
        //when
        when(ticketRepository.findById(any())).thenReturn(Optional.empty());
        //then
        assertThrows(NotFoundException.class, () -> orderService.createTicketOrder(FAKE_TICKET_ID, FAKE_TOKEN));
    }

    @Test
    public void testShouldThrowExceptionWhenTicketIsAlreadyBooked() {
        //given
        ticket.setOrderId(ObjectId.get().toString());
        //when
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticket));
        //then
        assertThrows(BadRequestException.class, () -> orderService.createTicketOrder(FAKE_TICKET_ID, FAKE_TOKEN));
    }

    @Test
    public void testShouldThrowExceptionWhenUserIsTicketOwner() {
        //given
        ticket.setUserId(FAKE_USER_ID);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticket));
        //then
        assertThrows(BadRequestException.class, () -> orderService.createTicketOrder(FAKE_TICKET_ID, FAKE_TOKEN));
    }

//    @Test
//    public void testShouldReturnUserOrders() {
//        //given
//        List<OrderEntity> orders = new ArrayList<>();
//        List<TicketEntity> tickets = new ArrayList<>();
//        IntStream.range(0, 10)
//                .forEach(x -> {
//                    tickets.add(new TicketEntity(ObjectId.get().toString(),
//                            FAKE_TICKET_TITLE,
//                            FAKE_TICKET_PRICE,
//                            FAKE_USER_ID_2,
//                            ObjectId.get().toString()));
//                });
//        IntStream.range(0, 10)
//                .forEach(x -> {
//                    orders.add(new OrderEntity(ObjectId.get().toString(),
//                            FAKE_USER_ID,
//                            OrderStatus.CREATED,
//                            LocalDateTime.now(),
//                            tickets.get(x)));
//                });
//        //when
//        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
//        when(orderRepository.findAllByUserId(FAKE_USER_ID)).thenReturn(orders);
//        //then
//        int firstOrderIndex = 0;
//        List<OrderResponse> userOrders = orderService.getTicketOrdersForUser(FAKE_TOKEN);
//        assertEquals(userOrders.size(), orders.size());
//        assertEquals(userOrders.get(firstOrderIndex).getOrderStatus(), orders.get(firstOrderIndex).getOrderStatus());
//        assertEquals(userOrders.get(firstOrderIndex).getExpiration(), orders.get(firstOrderIndex).getExpiresAt());
//    }

    @Test
    public void testShouldReturnOrderWhenUserIsItsOwner() {
        //given
        order.setUserId(FAKE_USER_ID);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        //then
        OrderResponse orderResponse = orderService.getTicketOrderForUser(FAKE_TOKEN, FAKE_ORDER_ID);
        assertEquals(orderResponse.getId(), order.getId());
        assertEquals(orderResponse.getTicket().getTicketId(), order.getTicket().getId());
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
        assertEquals(orderResponse.getExpiration(), order.getExpiresAt());
    }

    @Test
    public void testShouldNotReturnOrderWhenUserIsNotItsOwner() {
        //given
        order.setUserId(FAKE_USER_ID_2);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        //then
        assertThrows(NotFoundException.class, () -> orderService.getTicketOrderForUser(FAKE_TOKEN, FAKE_USER_ID));
    }

    @Test
    public void testShouldNotReturnOrderWhenOrderNotFound() {
        //given
        order.setUserId(FAKE_USER_ID_2);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        //then
        assertThrows(NotFoundException.class, () -> orderService.getTicketOrderForUser(FAKE_TOKEN, FAKE_USER_ID));
    }

    @Test
    public void testShouldDeleteOrderWhenUserIsOwner() {
        //given
        order.setUserId(FAKE_USER_ID);
        ticket.setOrderId(order.getId());
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticket));
        //then
        orderService.cancelOrder(FAKE_TOKEN, FAKE_USER_ID);
        verify(orderRepository, times(1)).delete(order);
        assertEquals(order.getOrderStatus(), OrderStatus.CANCELLED);
    }

    @Test
    public void testShouldNotDeleteOrderWhenOrderNotFound() {
        //given
        order.setUserId(FAKE_USER_ID);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        //then
        assertThrows(NotFoundException.class, () -> orderService.cancelOrder(FAKE_TOKEN, FAKE_USER_ID));
    }

    @Test
    public void testShouldNotDeleteOrderWhenOrderNotBelongsToUser() {
        //given
        order.setUserId(FAKE_USER_ID_2);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        //then
        assertThrows(NotFoundException.class, () -> orderService.cancelOrder(FAKE_TOKEN, FAKE_USER_ID));
    }
}