package com.spring.exercise.unittests.service;

import com.spring.exercise.entity.OrderEntity;
import com.spring.exercise.entity.PaymentEntity;
import com.spring.exercise.entity.TicketEntity;
import com.spring.exercise.exceptions.BadRequestException;
import com.spring.exercise.exceptions.NotFoundException;
import com.spring.exercise.integrationtests.BaseIntegrationTests;
import com.spring.exercise.model.payment.PaymentRequest;
import com.spring.exercise.model.payment.PaymentResponse;
import com.spring.exercise.model.payment.PaymentVendorDTO;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.repository.PaymentRepository;
import com.spring.exercise.service.PaymentServiceImpl;
import com.spring.exercise.service.PaymentVendorStripe;
import com.spring.exercise.utils.JwtUtils;
import com.spring.exercise.utils.OrderStatus;
import com.spring.exercise.utils.TicketDiscountCalculator;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTests extends BaseIntegrationTests {

    @InjectMocks
    private PaymentServiceImpl paymentService;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    PaymentVendorStripe paymentVendorStripe;
    @Mock
    TicketDiscountCalculator ticketDiscountCalculator;

    private OrderEntity orderEntity;
    private TicketEntity ticketEntity;
    private PaymentEntity paymentEntity;
    private PaymentVendorDTO paymentVendorDTO;
    private BigDecimal discount;

    private final static String FAKE_TOKEN = "fake_token";
    private final static String FAKE_USER_ID = "fake_user_id";
    private final static String FAKE_ORDER_ID = "fake_order_id";
    private final static String FAKE_TICKET_TITLE = "fake_title";
    private final static String FAKE_STRIPE_ID = "fake_stripe_id";
    private final static String FAKE_PAYMENT_ID = "fake_payment_id";
    private final static BigDecimal FAKE_TICKET_PRICE = new BigDecimal(12);

    public PaymentServiceTests() {
    }

    @BeforeEach
    public void setUp() {
        ticketEntity = new TicketEntity();
        ticketEntity.setId(ObjectId.get().toString());
        ticketEntity.setUserId(FAKE_USER_ID);
        ticketEntity.setTitle(FAKE_TICKET_TITLE);
        ticketEntity.setPrice(FAKE_TICKET_PRICE);

        orderEntity = OrderEntity.builder()
                .orderStatus(OrderStatus.CREATED)
                .ticket(ticketEntity)
                .id(FAKE_ORDER_ID)
                .expiresAt(LocalDateTime.now())
                .userId(FAKE_USER_ID).build();

        paymentEntity = PaymentEntity.builder()
                .orderId(FAKE_ORDER_ID)
                .id(ObjectId.get().toString())
                .stripeId(FAKE_STRIPE_ID).build();

        paymentVendorDTO = new PaymentVendorDTO();
        paymentVendorDTO.setPaymentId(FAKE_PAYMENT_ID);
    }

    @Test
    public void testCreatePaymentShouldSuccessWhenRequestIsCorrectAndDiscountNotApllied() {
        //given
        String fakeOrderId = FAKE_ORDER_ID;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToken("tok_visa");
        paymentRequest.setOrderId(fakeOrderId);
        discount = BigDecimal.ZERO;
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(fakeOrderId)).thenReturn(Optional.of(orderEntity));
        when(paymentRepository.save(any())).thenReturn(paymentEntity);
        when(paymentVendorStripe.pay(any())).thenReturn(paymentVendorDTO);
        when(ticketDiscountCalculator.calculateDiscount(any(), any())).thenReturn(discount);
        //then
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest, FAKE_TOKEN);
        assertEquals(paymentResponse.getPaymentId(), paymentEntity.getId());
    }

    @Test
    public void testCreatePaymentShouldSuccessWhenRequestIsCorrectAndDiscountApllied() {
        //given
        String fakeOrderId = FAKE_ORDER_ID;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToken("tok_visa");
        paymentRequest.setOrderId(fakeOrderId);
        discount = BigDecimal.valueOf(5);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(FAKE_USER_ID);
        when(orderRepository.findById(fakeOrderId)).thenReturn(Optional.of(orderEntity));
        when(paymentRepository.save(any())).thenReturn(paymentEntity);
        when(paymentVendorStripe.pay(any())).thenReturn(paymentVendorDTO);
        when(ticketDiscountCalculator.calculateDiscount(any(), any())).thenReturn(discount);
        //then
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest, FAKE_TOKEN);
        assertEquals(paymentResponse.getPaymentId(), paymentEntity.getId());
    }


    @Test
    public void testCreatePaymentShouldNotSuccessWhenUserIsNotOrderOwner() {
        //given
        String fakeOrderId = FAKE_ORDER_ID;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToken("tok_visa");
        paymentRequest.setOrderId(fakeOrderId);
        //when
        when(jwtUtils.fetchUserIdFromToken(any())).thenReturn(null);
        when(orderRepository.findById(fakeOrderId)).thenReturn(Optional.of(orderEntity));
        //then
        assertThrows(BadRequestException.class, () -> paymentService.createPayment(paymentRequest, FAKE_TOKEN));
    }

    @Test
    public void testCreatePaymentShouldNotSuccessWhenOrderNotFound() {
        //given
        String fakeOrderId = FAKE_ORDER_ID;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToken("tok_visa");
        paymentRequest.setOrderId(fakeOrderId);
        //when
        when(orderRepository.findById(fakeOrderId)).thenReturn(Optional.empty());
        //then
        assertThrows(NotFoundException.class, () -> paymentService.createPayment(paymentRequest, FAKE_TOKEN));
    }

    @Test
    public void testCreatePaymentShouldNotSuccessWhenOrderStatusIsCancelled() {
        //given
        String fakeOrderId = FAKE_ORDER_ID;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToken("tok_visa");
        paymentRequest.setOrderId(fakeOrderId);
        orderEntity.setOrderStatus(OrderStatus.CANCELLED);
        //when
        when(orderRepository.findById(fakeOrderId)).thenReturn(Optional.of(orderEntity));
        //then
        assertThrows(BadRequestException.class, () -> paymentService.createPayment(paymentRequest, FAKE_TOKEN));
    }

    @Test
    public void testCreatePaymentShouldNotSuccessWhenOrderStatusIsCompleted() {
        //given
        String fakeOrderId = FAKE_ORDER_ID;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToken("tok_visa");
        paymentRequest.setOrderId(fakeOrderId);
        orderEntity.setOrderStatus(OrderStatus.COMPLETED);
        //when
        when(orderRepository.findById(fakeOrderId)).thenReturn(Optional.of(orderEntity));
        //then
        assertThrows(BadRequestException.class, () -> paymentService.createPayment(paymentRequest, FAKE_TOKEN));
    }
}