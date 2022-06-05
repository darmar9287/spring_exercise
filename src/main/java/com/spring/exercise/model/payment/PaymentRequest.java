package com.spring.exercise.model.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    @NotBlank
    private String token;
    @NotBlank
    private String orderId;
}