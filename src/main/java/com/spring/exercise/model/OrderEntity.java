package com.spring.exercise.model;

import com.spring.exercise.utils.OrderStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    @Id
    private String id;
    private String userId;
    private OrderStatus orderStatus;
    private LocalDateTime expiresAt;
    @DBRef
    private TicketEntity ticket;
}
