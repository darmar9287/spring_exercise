package com.spring.exercise.model.order;

import com.spring.exercise.utils.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private OrderStatus orderStatus;
    private LocalDateTime expiration;
    private TicketOrderResponse ticket;

    @AllArgsConstructor
    @Data
    public static class TicketOrderResponse {
        String ticketId;
        BigDecimal price;
    }
}