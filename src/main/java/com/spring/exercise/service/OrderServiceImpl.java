package com.spring.exercise.service;

import com.spring.exercise.controller.model.order.OrderResponse;
import com.spring.exercise.exceptions.*;
import com.spring.exercise.model.OrderEntity;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.JwtUtils;
import com.spring.exercise.utils.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${expiration.in.seconds}")
    private long expirationSeconds;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final JwtUtils jwtUtils;

    public OrderResponse createTicketOrder(String ticketId, String token) {
        Optional<TicketEntity> optionalTicket = ticketRepository.findById(ticketId);
        if (optionalTicket.isEmpty()) {
            log.warn("Did not find ticket with id " + ticketId);
            throw new TicketNotFoundException(ticketId);
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
        foundTicket.setOrderId(ObjectId.get().toString());
        ticketRepository.save(foundTicket);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationSeconds);
        OrderEntity order = OrderEntity.builder()
                .expiresAt(expiresAt)
                .orderStatus(OrderStatus.CREATED)
                .userId(userId)
                .ticket(foundTicket)
                .build();
        orderRepository.save(order);
        BigDecimal price = foundTicket.getPrice();

        return OrderResponse.builder().id(order.getId())
                .orderStatus(order.getOrderStatus())
                .ticket(new OrderResponse.TicketOrderResponse(ticketId, price))
                .expiration(expiresAt).build();
    }

    public List<OrderResponse> getTicketOrdersForUser(String token) {
        String userId = jwtUtils.fetchUserIdFromToken(token);
        List<OrderEntity> orders = orderRepository.getTicketOrdersForUser(userId);
        List<OrderResponse> orderList = orders.stream()
                .map(o -> OrderResponse.builder()
                        .id(o.getId())
                        .orderStatus(o.getOrderStatus())
                        .expiration(o.getExpiresAt())
                        .ticket(new OrderResponse.TicketOrderResponse(o.getTicket().getTitle(), o.getTicket().getPrice()))
                        .build())
                .collect(Collectors.toList());
        return orderList;
    }

    public OrderResponse getTicketOrderForUser(String token, String orderId) {
        String userId = jwtUtils.fetchUserIdFromToken(token);
        Optional<OrderEntity> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            log.warn("Did not find order with id " + orderId);
            throw new OrderNotFoundException(orderId);
        }
        OrderEntity foundOrder = order.get();
        if (!checkIfUserIsOrderOwner(userId, foundOrder)) {
            log.warn("User is not owner for order with id " + orderId);
            throw new OrderNotFoundException(orderId);
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
        Optional<OrderEntity> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            log.warn("Did not find order with id " + orderId);
            throw new OrderNotFoundException(orderId);
        }
        OrderEntity foundOrder = order.get();
        if (!checkIfUserIsOrderOwner(userId, foundOrder)) {
            log.warn("User is not owner for order with id " + orderId);
            throw new OrderNotFoundException(orderId);
        }
        if (foundOrder.getOrderStatus() == OrderStatus.CREATED) {
            foundOrder.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.delete(foundOrder);
            return;
        }
        String exceptionMessage = "Cannot cancel order with id: " + orderId + ". Status is COMPLETED/CANCELLED";
        throw new BadRequestException(exceptionMessage);
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