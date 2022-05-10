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
public class TicketCreateResponse {
        private String id;
        private String title;
        private BigDecimal price;
        private String userId;
}
