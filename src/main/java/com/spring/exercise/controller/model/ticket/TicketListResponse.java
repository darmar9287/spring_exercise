package com.spring.exercise.controller.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketListResponse {
    private List<TicketDTO> tickets;
    private int currentPage;
    private long totalTickets;
    private int totalPages;
}
