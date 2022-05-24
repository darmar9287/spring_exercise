package com.spring.exercise.controller.model.order;

import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.utils.OrderStatus;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Builder
public class OrderDTO {
    private final String id;
    private final String userId;
    private final OrderStatus orderStatus;
    private final LocalDateTime expiresAt;
    private final TicketEntity ticket;
}