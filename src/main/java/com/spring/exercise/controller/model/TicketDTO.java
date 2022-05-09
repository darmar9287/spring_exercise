package com.spring.exercise.controller.model;

import com.spring.exercise.model.TicketEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class TicketDTO {

    private final String id;
    private final String title;
    private final BigDecimal price;

    public static TicketDTO mapFromEntity(TicketEntity ticketEntity) {
        return new TicketDTO(ticketEntity.getId(), ticketEntity.getTitle(), ticketEntity.getPrice());
    }
}
