package com.spring.exercise.controller.model;

import com.spring.exercise.model.TicketEntity;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.text.Bidi;

@Data
@RequiredArgsConstructor
@Builder
public class TicketDTO {

    private final String id;
    private final String title;
    private final BigDecimal price;
    private final String userId;
}
