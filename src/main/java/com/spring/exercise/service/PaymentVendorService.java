package com.spring.exercise.service;

import com.spring.exercise.model.payment.PaymentInput;
import com.spring.exercise.model.payment.PaymentVendorDTO;

public interface PaymentVendorService {
   PaymentVendorDTO pay(PaymentInput paymentInput);
}