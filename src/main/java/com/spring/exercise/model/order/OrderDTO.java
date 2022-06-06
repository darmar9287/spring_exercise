package com.spring.exercise.model.order;

import com.spring.exercise.model.ticket.TicketDTO;
import com.spring.exercise.entity.OrderEntity;
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
    private final TicketDTO ticket;

    public static OrderDTO mapFromEntity(OrderEntity orderEntity) {
        return new OrderDTOBuilder().id(orderEntity.getId())
                .userId(orderEntity.getUserId())
                .expiresAt(orderEntity.getExpiresAt())
                .orderStatus(orderEntity.getOrderStatus())
                .ticket(TicketDTO.mapFromEntity(orderEntity.getTicket()))
                .build();
    }
}