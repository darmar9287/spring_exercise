package com.spring.exercise.service;

import com.spring.exercise.model.payment.PaymentInput;
import com.spring.exercise.model.payment.PaymentVendorDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnProperty(name = "payment.vendor", havingValue = "mock")
public class PaymentServiceMock implements PaymentVendorService {
    @Override
    public PaymentVendorDTO pay(PaymentInput paymentInput) {
        return new PaymentVendorDTO(UUID.randomUUID().toString());
    }
}