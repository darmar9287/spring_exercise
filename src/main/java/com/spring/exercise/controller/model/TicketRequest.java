package com.spring.exercise.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketRequest {

    private String id;
    @NotBlank
    private String title;
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;
}
