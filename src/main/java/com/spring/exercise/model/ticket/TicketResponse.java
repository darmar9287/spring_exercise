package com.spring.exercise.model.ticket;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
@Builder
public class TicketResponse {
    private final String id;
    private final String title;
    private final BigDecimal price;
    private final String userId;
}
