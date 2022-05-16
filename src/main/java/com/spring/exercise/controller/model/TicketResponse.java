package com.spring.exercise.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketResponse {
        private String id;
        private String title;
        private BigDecimal price;
        private String userId;

    public static TicketResponse mapFromDTO(TicketDTO ticket) {
        return new TicketResponseBuilder().id(ticket.getId())
                .title(ticket.getTitle())
                .price(ticket.getPrice())
                .userId(ticket.getUserId()).build();
    }
}
