package com.spring.exercise.unittests.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.spring.exercise.configuration.SQSConnectionConfiguration;
import com.spring.exercise.entity.OrderEntity;
import com.spring.exercise.entity.TicketEntity;
import com.spring.exercise.exceptions.BadRequestException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.integrationtests.BaseIntegrationTests;
import com.spring.exercise.model.order.OrderListResponse;
import com.spring.exercise.model.order.OrderResponse;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.service.OrderServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import com.spring.exercise.utils.OrderStatus;
import com.spring.exercise.utils.TicketDiscountCalculator;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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
    @Mock
    private TicketDiscountCalculator ticketDiscountCalculator;

    private TicketEntity ticket;
    private OrderEntity order;

    private final static String FAKE_TOKEN = "fake_token";
    private final static String FAKE_TICKET_ID = "fake_ticket_id";
    private final static String TICKET_DESCRIPTION = "fake_description_";
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
    public void testCreateOrderShouldSuccessWhenRequestIsCorrectAndNoDiscountApplied() {
        //given
        int discountPercentage = 0;
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID_2);
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticket));
        when(sqsConnectionConfiguration.amazonSQSClient()).thenReturn(sqsClient);
        when(sqsClient.getQueueUrl("order-expiration")).thenReturn(new GetQueueUrlResult());
        when(ticketDiscountCalculator.calculateDiscountPercentage(any())).thenReturn(discountPercentage);
        //then
        OrderResponse orderResponse = orderService.createTicketOrder(FAKE_TICKET_ID, FAKE_TOKEN);
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
        assertEquals(orderResponse.getTicket().getTicketId(), order.getTicket().getId());
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
        assertEquals(orderResponse.getTicket().getPrice(), order.getTicket().getPrice());
        assertEquals(discountPercentage, orderResponse.getTicket().getDiscountPercentage());
    }

    @Test
    public void testCreateOrderShouldSuccessWhenRequestIsCorrectAndDiscountApplied() {
        //given
        int discountPercentage = 5;
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID_2);
        when(ticketRepository.findById(any())).thenReturn(Optional.of(ticket));
        when(sqsConnectionConfiguration.amazonSQSClient()).thenReturn(sqsClient);
        when(sqsClient.getQueueUrl("order-expiration")).thenReturn(new GetQueueUrlResult());
        when(ticketDiscountCalculator.calculateDiscountPercentage(any())).thenReturn(discountPercentage);
        //then
        OrderResponse orderResponse = orderService.createTicketOrder(FAKE_TICKET_ID, FAKE_TOKEN);
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
        assertEquals(orderResponse.getTicket().getTicketId(), order.getTicket().getId());
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
        assertEquals(orderResponse.getTicket().getPrice(), order.getTicket().getPrice());
        assertEquals(discountPercentage, orderResponse.getTicket().getDiscountPercentage());
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

    @Test
    public void testShouldReturnUserOrders() {
        //given
        List<OrderEntity> orders = new ArrayList<>();
        List<TicketEntity> tickets = new ArrayList<>();
        IntStream.range(0, 10)
                .forEach(x -> {
                    tickets.add(new TicketEntity(ObjectId.get().toString(),
                            FAKE_TICKET_TITLE,
                            FAKE_TICKET_PRICE,
                            TICKET_DESCRIPTION + x,
                            FAKE_USER_ID_2,
                            ObjectId.get().toString()));
                });
        IntStream.range(0, 10)
                .forEach(x -> {
                    orders.add(new OrderEntity(ObjectId.get().toString(),
                            FAKE_USER_ID,
                            OrderStatus.CREATED,
                            LocalDateTime.now(),
                            tickets.get(x)));
                });
        Page<OrderEntity> page = new PageImpl<>(orders);
        int pageIndex = 0;
        int pageSize = 5;
        //when
        PageRequest paging = PageRequest.of(pageIndex, pageSize);
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findAllByUserId(FAKE_USER_ID, paging)).thenReturn(page);
        //then
        int firstOrderIndex = 0;
        OrderListResponse userOrders = orderService.getTicketOrdersForUser(FAKE_TOKEN, pageIndex, pageSize);
        assertEquals(userOrders.getOrders().size(), orders.size());
        assertEquals(userOrders.getOrders().get(firstOrderIndex).getOrderStatus(), orders.get(firstOrderIndex).getOrderStatus());
        assertEquals(userOrders.getOrders().get(firstOrderIndex).getExpiration(), orders.get(firstOrderIndex).getExpiresAt());
        assertEquals(paging.getPageSize(), 5);
    }

    @Test
    public void testShouldReturnOrderWhenUserIsItsOwnerAndNoDiscountApplied() {
        //given
        int discountPercentage = 0;
        order.setUserId(FAKE_USER_ID);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(ticketDiscountCalculator.calculateDiscountPercentage(any())).thenReturn(discountPercentage);
        //then
        OrderResponse orderResponse = orderService.getTicketOrderForUser(FAKE_TOKEN, FAKE_ORDER_ID);
        assertEquals(orderResponse.getId(), order.getId());
        assertEquals(orderResponse.getTicket().getTicketId(), order.getTicket().getId());
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
        assertEquals(orderResponse.getExpiration(), order.getExpiresAt());
        assertEquals(orderResponse.getTicket().getPrice(), order.getTicket().getPrice());
        assertEquals(discountPercentage, orderResponse.getTicket().getDiscountPercentage());
    }

    @Test
    public void testShouldReturnOrderWhenUserIsItsOwnerAndDiscountApplied() {
        //given
        int discountPercentage = 0;
        order.setUserId(FAKE_USER_ID);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(ticketDiscountCalculator.calculateDiscountPercentage(any())).thenReturn(discountPercentage);
        //then
        OrderResponse orderResponse = orderService.getTicketOrderForUser(FAKE_TOKEN, FAKE_ORDER_ID);
        assertEquals(orderResponse.getId(), order.getId());
        assertEquals(orderResponse.getTicket().getTicketId(), order.getTicket().getId());
        assertEquals(orderResponse.getOrderStatus(), order.getOrderStatus());
        assertEquals(orderResponse.getExpiration(), order.getExpiresAt());
        assertEquals(orderResponse.getTicket().getPrice(), order.getTicket().getPrice());
        assertEquals(discountPercentage, orderResponse.getTicket().getDiscountPercentage());
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

//    @Test
//    public void testShouldNotDeleteOrderWhenOrderNotBelongsToUser() {
//        //given
//        order.setUserId(FAKE_USER_ID_2);
//        //when
//        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
//        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
//        //then
//        assertThrows(NotFoundException.class, () -> orderService.cancelOrder(FAKE_TOKEN, FAKE_USER_ID));
//    }
}