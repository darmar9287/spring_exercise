package com.spring.exercise.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketListResponse {
    private List<TicketDTO> tickets;
    private int currentPage;
    private long totalTickets;
    private int totalPages;
}
