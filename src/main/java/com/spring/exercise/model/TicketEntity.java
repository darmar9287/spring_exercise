package com.spring.exercise.model;

import com.spring.exercise.utils.AppMessages;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Document(collection = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketEntity {
    @Id
    private String id;
    @NotBlank(message = AppMessages.TICKET_TITLE_BLANK_ERROR)
    private String title;
    @DecimalMin(value = "0.01", inclusive = false, message = AppMessages.TICKET_PRICE_TOO_LOW)
    private BigDecimal price;
    private String userId;
}