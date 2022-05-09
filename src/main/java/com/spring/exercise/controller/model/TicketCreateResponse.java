package com.spring.exercise.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketCreateResponse {
        private String id;
        private String title;
        private BigDecimal price;
        private String userId;

    public static TicketCreateResponse mapFromDTO(TicketDTO ticket, String userId) {
        return new TicketCreateResponse(ticket.getId(), ticket.getTitle(), ticket.getPrice(), userId);
    }
}
