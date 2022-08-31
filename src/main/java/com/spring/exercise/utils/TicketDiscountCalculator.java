package com.spring.exercise.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class TicketDiscountCalculator {
    private int discountPercentage;

    public int calculateDiscountPercentage(LocalDate currentDate) {
        if (currentDate.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
           return discountPercentage = 5;
        }
        discountPercentage = 0;
        return discountPercentage;
    }

    public BigDecimal calculateDiscount(LocalDate currentDate, BigDecimal currentPrice) {
        if (currentDate.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
            discountPercentage = 5;
            return currentPrice.subtract(currentPrice.multiply(BigDecimal.valueOf((double) discountPercentage / 100)));
        }
        return currentPrice;
    }
}