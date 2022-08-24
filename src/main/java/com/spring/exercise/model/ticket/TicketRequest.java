package com.spring.exercise.model.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketRequest {
    @NotBlank
    private String title;
    @DecimalMin(value = "0.5", inclusive = true)
    private BigDecimal price;
    @NotBlank
    @Size(max = 500)
    private String description;
}