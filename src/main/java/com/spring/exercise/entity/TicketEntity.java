package com.spring.exercise.entity;

import com.spring.exercise.utils.AppMessages;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Document(collection = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketEntity {
    @Id
    private String id;
    @NotBlank(message = AppMessages.BLANK_ERROR)
    private String title;
    @DecimalMin(value = "0.5", inclusive = false, message = AppMessages.TICKET_PRICE_TOO_LOW_ERROR)
    private BigDecimal price;
    @NonNull
    @Size(max = 500)
    private String description;
    private String userId;
    private String orderId;
}