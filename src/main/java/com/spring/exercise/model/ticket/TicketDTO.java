package com.spring.exercise.model.ticket;

import com.spring.exercise.entity.TicketEntity;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
@Builder
public class TicketDTO {
    private final String id;
    private final String title;
    private final BigDecimal price;
    private final String userId;
    private final String orderId;
    private final String description;

    public static TicketDTO mapFromEntity(TicketEntity ticketEntity) {
        return new TicketDTOBuilder().id(ticketEntity.getId())
                .title(ticketEntity.getTitle())
                .price(ticketEntity.getPrice())
                .description(ticketEntity.getDescription())
                .userId(ticketEntity.getUserId())
                .orderId(ticketEntity.getOrderId())
                .build();
    }
}