package com.spring.exercise.service;

import com.spring.exercise.entity.OrderEntity;
import com.spring.exercise.entity.PaymentEntity;
import com.spring.exercise.exceptions.BadRequestException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.model.order.OrderDTO;
import com.spring.exercise.model.payment.PaymentInput;
import com.spring.exercise.model.payment.PaymentRequest;
import com.spring.exercise.model.payment.PaymentResponse;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.repository.PaymentRepository;
import com.spring.exercise.utils.JwtUtils;
import com.spring.exercise.utils.OrderStatus;
import com.spring.exercise.utils.TicketDiscountCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl {
    private final PaymentVendorService paymentVendorService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TicketDiscountCalculator ticketDiscountCalculator;
    private final JwtUtils jwtUtils;

    public PaymentResponse createPayment(PaymentRequest paymentRequest, String token) {
        String orderId = paymentRequest.getOrderId();
        String stripeToken = paymentRequest.getToken();
        Optional<OrderEntity> order = orderRepository.findById(orderId);
        String errorMessage = String.format("Order with id: %s was not found", orderId);
        if (order.isEmpty()) {
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        OrderEntity foundOrder = order.get();
        errorMessage = String.format("Order with id: %s is %s", orderId, foundOrder.getOrderStatus().name());
        if (foundOrder.getOrderStatus() == OrderStatus.CANCELLED
                || foundOrder.getOrderStatus() == OrderStatus.COMPLETED) {
            log.error(errorMessage);
            throw new BadRequestException(errorMessage);
        }

        errorMessage = String.format("Order with id: %s does not belong to user", orderId);
        String userIdFromToken = jwtUtils.fetchUserIdFromToken(token);
        if (!foundOrder.getUserId().equals(userIdFromToken)) {
            log.error(errorMessage);
            throw new BadRequestException(errorMessage);
        }
        OrderDTO orderDTO = OrderDTO.mapFromEntity(foundOrder);
        BigDecimal amount = ticketDiscountCalculator.calculateDiscount(LocalDate.now(), orderDTO.getTicket().getPrice());
        PaymentInput paymentInput =  new PaymentInput();
        paymentInput.setAmount(amount);
        paymentInput.setStripeToken(stripeToken);
        var paymentVendorDTO = paymentVendorService.pay(paymentInput);
        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(orderId);
        payment.setStripeId(paymentVendorDTO.getPaymentId());
        String paymentId = paymentRepository.save(payment).getId();
        log.info("Setting order status for order id:{} to {}", orderId, OrderStatus.COMPLETED.name());
        foundOrder.setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.save(foundOrder);
        return new PaymentResponse(paymentId);
    }
}