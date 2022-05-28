package com.spring.exercise.service;

import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.spring.exercise.configuration.SQSConnectionConfiguration;
import com.spring.exercise.controller.model.order.OrderResponse;
import com.spring.exercise.exceptions.BadRequestException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.model.OrderEntity;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.JwtUtils;
import com.spring.exercise.utils.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl {

    @Value("${order.expiration.in.seconds}")
    private int expirationSeconds;
    @Value("${application.sqs.host}")
    String destination;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final SQSConnectionConfiguration queueConfiguration;

    private final JwtUtils jwtUtils;


    public OrderResponse createTicketOrder(String ticketId, String token) {
        Optional<TicketEntity> optionalTicket = ticketRepository.findById(ticketId);
        if (optionalTicket.isEmpty()) {
            log.warn("Did not find ticket with id " + ticketId);
            throw new NotFoundException("Ticket with id " + ticketId + " was not found");
        }

        TicketEntity foundTicket = optionalTicket.get();
        if (checkIfTicketIsAlreadyBooked(foundTicket)) {
            log.warn("Ticket with id %s is already booked", ticketId);
            throw new BadRequestException(AppMessages.TICKET_ALREADY_BOOKED_ERROR);
        }
        String userId = jwtUtils.fetchUserIdFromToken(token);
        if (checkIfUserIsTicketOwner(userId, foundTicket)) {
            log.warn("User tried to book his own ticket");
            throw new BadRequestException(AppMessages.OWN_TICKET_PURCHASE_ERROR);
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationSeconds);
        OrderEntity order = OrderEntity.builder()
                .expiresAt(expiresAt)
                .orderStatus(OrderStatus.CREATED)
                .userId(userId)
                .ticket(foundTicket)
                .build();
        orderRepository.save(order);
        foundTicket.setOrderId(order.getId());
        ticketRepository.save(foundTicket);
        BigDecimal price = foundTicket.getPrice();

        var sqsClient = queueConfiguration.amazonSQSClient();
        GetQueueUrlResult queueUrl = sqsClient.getQueueUrl("order-expiration");
        final SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.withMessageBody(order.getId());
        sendMessageRequest.withQueueUrl(queueUrl.getQueueUrl());
        sendMessageRequest.withDelaySeconds(expirationSeconds);
        sqsClient.sendMessage(sendMessageRequest);

        return OrderResponse.builder().id(order.getId())
                .orderStatus(order.getOrderStatus())
                .ticket(new OrderResponse.TicketOrderResponse(ticketId, price))
                .expiration(expiresAt).build();
    }

    public List<OrderResponse> getTicketOrdersForUser(String token) {
        String userId = jwtUtils.fetchUserIdFromToken(token);
        List<OrderEntity> orders = orderRepository.findAllByUserId(userId);
        return orders.stream()
                .map(o -> OrderResponse.builder()
                        .id(o.getId())
                        .orderStatus(o.getOrderStatus())
                        .expiration(o.getExpiresAt())
                        .ticket(new OrderResponse.TicketOrderResponse(o.getTicket().getTitle(), o.getTicket().getPrice()))
                        .build())
                .collect(Collectors.toList());
    }

    public OrderResponse getTicketOrderForUser(String token, String orderId) {
        String userId = jwtUtils.fetchUserIdFromToken(token);
        Optional<OrderEntity> order = orderRepository.findById(orderId);
        String errorMessage = null;
        if (order.isEmpty()) {
            errorMessage = "Order with id " + orderId + " was not found";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        OrderEntity foundOrder = order.get();
        if (!checkIfUserIsOrderOwner(userId, foundOrder)) {
            errorMessage = "Order with id " + orderId + " was not found";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        return OrderResponse.builder()
                .id(orderId)
                .orderStatus(foundOrder.getOrderStatus())
                .expiration(foundOrder.getExpiresAt())
                .ticket(new OrderResponse.TicketOrderResponse(foundOrder.getTicket().getId(), foundOrder.getTicket().getPrice()))
                .build();
    }

    public void cancelOrder(String token, String orderId) {
        String userId = jwtUtils.fetchUserIdFromToken(token);
        String errorMessage = null;
        Optional<OrderEntity> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            errorMessage = "Order with id " + orderId + " was not found";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        OrderEntity foundOrder = order.get();
        if (!checkIfUserIsOrderOwner(userId, foundOrder)) {
            errorMessage = "Order with id " + orderId + " was not found";
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        if (foundOrder.getOrderStatus() != OrderStatus.CREATED) {
            String exceptionMessage = "Cannot cancel order with id: " + orderId + ". Status is " + foundOrder.getOrderStatus();
            throw new BadRequestException(exceptionMessage);
        }
        foundOrder.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.delete(foundOrder);
    }

    @SqsListener("order-expiration")
    public void getMessage(String message) {
        log.info("Queued orderId: " + message);
        Optional<OrderEntity> order = orderRepository.findById(message);
        if (order.isPresent() && order.get().getOrderStatus() == OrderStatus.CREATED) {
            order.get().setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order.get());
        }
    }

    private boolean checkIfTicketIsAlreadyBooked(TicketEntity foundTicket) {
        return foundTicket.getOrderId() != null;
    }

    private boolean checkIfUserIsTicketOwner(String userId, TicketEntity foundTicket) {
        return foundTicket.getUserId().equals(userId);
    }

    private boolean checkIfUserIsOrderOwner(String userId, OrderEntity foundOrder) {
        return foundOrder.getUserId().equals(userId);
    }
}