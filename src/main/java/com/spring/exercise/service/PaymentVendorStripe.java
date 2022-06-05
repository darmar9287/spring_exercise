package com.spring.exercise.service;

import com.spring.exercise.exceptions.BadRequestException;
import com.spring.exercise.model.payment.PaymentInput;
import com.spring.exercise.model.payment.PaymentVendorDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "payment.vendor", havingValue = "stripe")
@Slf4j
public class PaymentVendorStripe implements PaymentVendorService{
    @Value("${stripe.secret_key}")
    private String apiKey;

    @Override
    public PaymentVendorDTO pay(PaymentInput paymentInput) {
        Stripe.apiKey = apiKey;
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", (paymentInput.getAmount().longValue() * 100));
        chargeParams.put("currency", "USD");
        chargeParams.put("source", paymentInput.getStripeToken());
        String stripeId = null;
        Charge charge = null;
        try {
            charge = Charge.create(chargeParams);
            stripeId = charge.getId();
        } catch (StripeException e) {
            String errorMessage = "Failed to charge user account";
            log.error("Error occured while trying to charge client " + e.getMessage());
            throw new BadRequestException(errorMessage);
        }

        return new PaymentVendorDTO(stripeId);
    }
}