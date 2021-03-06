package com.spring.exercise.model.ticket;

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
    private List<TicketResponse> tickets;
    private int currentPage;
    private long totalTickets;
    private int totalPages;
}
