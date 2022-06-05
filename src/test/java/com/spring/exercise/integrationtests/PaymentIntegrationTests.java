package com.spring.exercise.integrationtests;

import com.spring.exercise.entity.OrderEntity;
import com.spring.exercise.entity.PaymentEntity;
import com.spring.exercise.model.order.OrderCreateRequest;
import com.spring.exercise.model.payment.PaymentRequest;
import com.spring.exercise.model.ticket.TicketRequest;
import com.spring.exercise.model.user.AuthRequest;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.repository.PaymentRepository;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.utils.AppMessages;
import com.spring.exercise.utils.JwtUtils;
import com.spring.exercise.utils.OrderStatus;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
public class PaymentIntegrationTests extends BaseIntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private JwtUtils jwtUtils;

    private PaymentRequest paymentRequest;
    private TicketRequest ticketRequest;
    private static final String TICKET_TITLE = "ticket_title";
    private static final BigDecimal TICKET_PRICE = new BigDecimal(13);
    private static final String STRIPE_TOKEN = "tok_visa";

    @BeforeEach
    public void setUp() {
        ticketRequest = new TicketRequest(TICKET_TITLE, TICKET_PRICE);
        authRequest = new AuthRequest(USER_NAME, USER_PASSWORD);
    }

    @AfterEach
    public void tearDown() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    public void shouldResponseWith201WhenPaymentIsCompleted() throws Exception {
        MvcResult ticketOwner = createDefaultUser();
        AuthRequest authRequestUser = new AuthRequest("user_order@mail.com", "pass");
        MvcResult user = createCustomUser(authRequestUser);
        int tokenIndexStart = 7;
        String token = fetchToken(ticketOwner);
        String tokenValue = token.substring(tokenIndexStart);
        String ticketOwnerUserId = jwtUtils.extractId(tokenValue);
        MvcResult result = createTicketForUser(ticketOwnerUserId, token, ticketRequest);
        token = fetchToken(user);
        JSONObject jsonObj = new JSONObject(result.getResponse().getContentAsString());
        String ticketId = jsonObj.getString("id");
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(ticketId);
        String expectedOrderStatus = "CREATED";
        result = createOrder(orderCreateRequest, token);
        String orderId = new JSONObject(result.getResponse().getContentAsString()).getString("id");
        OrderEntity order = orderRepository.findById(orderId).get();
        assertEquals(order.getOrderStatus().name(), expectedOrderStatus);

        paymentRequest = new PaymentRequest(STRIPE_TOKEN, orderId);
        MvcResult paymentCreateResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/payments/create")
                        .content(mapToJson(paymentRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").exists())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        jsonObj = new JSONObject(paymentCreateResult.getResponse().getContentAsString());
        String paymentId = jsonObj.getString("paymentId");
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId).get();
        assertEquals(paymentEntity.getId(), paymentId);
        assertEquals(paymentEntity.getOrderId(), orderId);

        expectedOrderStatus = OrderStatus.COMPLETED.name();
        String completedOrdedStatus = orderRepository.findById(orderId).get().getOrderStatus().name();
        assertEquals(completedOrdedStatus, expectedOrderStatus);
    }

    @Test
    public void shouldResponseWith401WhenUserIsNotAuthenticated() throws Exception {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(ObjectId.get().toString());
        paymentRequest.setToken(STRIPE_TOKEN);
        String fakeToken = "fake_token";
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", fakeToken)
                        .content(mapToJson(paymentRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value(AppMessages.NOT_AUTHORIZED_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith404WhenOrderNotFound() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        String wrongOrderId = "wrong_order_id";
        String expectedError = "Order with id: " + wrongOrderId + " was not found";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToken(STRIPE_TOKEN);
        paymentRequest.setOrderId(wrongOrderId);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(paymentRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value(expectedError))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith400WhenOrderIdIsBlank() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToken(STRIPE_TOKEN);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(paymentRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(AppMessages.BLANK_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith400WhenTokenIsBlank() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId("fake_order_id");
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(paymentRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(AppMessages.BLANK_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith404WhenOrderDoesNotBelongToUser() throws Exception {
        MvcResult ticketOwner = createDefaultUser();
        AuthRequest authRequestFirstUser = new AuthRequest("first_user_order@mail.com", "pass");
        MvcResult firstUser = createCustomUser(authRequestFirstUser);
        AuthRequest authRequestSecondUser = new AuthRequest("second_user_order@mail.com", "pass");
        MvcResult secondUser = createCustomUser(authRequestSecondUser);

        int tokenIndexStart = 7;
        String token = fetchToken(ticketOwner);
        String tokenValue = token.substring(tokenIndexStart);
        String ticketOwnerUserId = jwtUtils.extractId(tokenValue);
        MvcResult ticketCreateResult = createTicketForUser(ticketOwnerUserId, token, ticketRequest);
        JSONObject jsonObj = new JSONObject(ticketCreateResult.getResponse().getContentAsString());
        String ticketId = jsonObj.getString("id");
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(ticketId);

        token = fetchToken(firstUser);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(orderCreateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        jsonObj = new JSONObject(result.getResponse().getContentAsString());
        String orderId = jsonObj.getString("id");
        token = fetchToken(secondUser);
        String expectedErrorMessage = "Order with id: " + orderId + " does not belong to user";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(orderId);
        paymentRequest.setToken(STRIPE_TOKEN);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(paymentRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(expectedErrorMessage))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith400WhenOrderStatusIsCompleted() throws Exception {
        MvcResult ticketOwner = createDefaultUser();
        AuthRequest authRequestUser = new AuthRequest("user_order@mail.com", "pass");
        MvcResult user = createCustomUser(authRequestUser);
        int tokenIndexStart = 7;
        String token = fetchToken(ticketOwner);
        String tokenValue = token.substring(tokenIndexStart);
        String ticketOwnerUserId = jwtUtils.extractId(tokenValue);
        MvcResult result = createTicketForUser(ticketOwnerUserId, token, ticketRequest);
        token = fetchToken(user);
        JSONObject jsonObj = new JSONObject(result.getResponse().getContentAsString());
        String ticketId = jsonObj.getString("id");
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(ticketId);
        String expectedOrderStatus = "CREATED";
        result = createOrder(orderCreateRequest, token);
        String orderId = new JSONObject(result.getResponse().getContentAsString()).getString("id");
        OrderEntity order = orderRepository.findById(orderId).get();
        assertEquals(order.getOrderStatus().name(), expectedOrderStatus);

        OrderEntity foundOrder = orderRepository.findById(orderId).get();
        foundOrder.setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.save(foundOrder);

        paymentRequest = new PaymentRequest(STRIPE_TOKEN, orderId);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/payments/create")
                        .content(mapToJson(paymentRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        expectedOrderStatus = OrderStatus.COMPLETED.name();
        String completedOrdedStatus = orderRepository.findById(orderId).get().getOrderStatus().name();
        assertEquals(completedOrdedStatus, expectedOrderStatus);
    }

    @Test
    public void shouldResponseWith400WhenOrderStatusIsCancelled() throws Exception {
        MvcResult ticketOwner = createDefaultUser();
        AuthRequest authRequestUser = new AuthRequest("user_order@mail.com", "pass");
        MvcResult user = createCustomUser(authRequestUser);
        int tokenIndexStart = 7;
        String token = fetchToken(ticketOwner);
        String tokenValue = token.substring(tokenIndexStart);
        String ticketOwnerUserId = jwtUtils.extractId(tokenValue);
        MvcResult result = createTicketForUser(ticketOwnerUserId, token, ticketRequest);
        token = fetchToken(user);
        JSONObject jsonObj = new JSONObject(result.getResponse().getContentAsString());
        String ticketId = jsonObj.getString("id");
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(ticketId);
        String expectedOrderStatus = "CREATED";
        result = createOrder(orderCreateRequest, token);
        String orderId = new JSONObject(result.getResponse().getContentAsString()).getString("id");
        OrderEntity order = orderRepository.findById(orderId).get();
        assertEquals(order.getOrderStatus().name(), expectedOrderStatus);

        OrderEntity foundOrder = orderRepository.findById(orderId).get();
        foundOrder.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(foundOrder);

        paymentRequest = new PaymentRequest(STRIPE_TOKEN, orderId);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/payments/create")
                        .content(mapToJson(paymentRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        expectedOrderStatus = OrderStatus.CANCELLED.name();
        String completedOrdedStatus = orderRepository.findById(orderId).get().getOrderStatus().name();
        assertEquals(completedOrdedStatus, expectedOrderStatus);

    }

    private MvcResult createOrder(OrderCreateRequest orderCreateRequest, String token) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .content(mapToJson(orderCreateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }
}