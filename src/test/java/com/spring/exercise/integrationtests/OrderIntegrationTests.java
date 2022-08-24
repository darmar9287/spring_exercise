package com.spring.exercise.integrationtests;

import com.spring.exercise.entity.OrderEntity;
import com.spring.exercise.entity.TicketEntity;
import com.spring.exercise.model.order.OrderCreateRequest;
import com.spring.exercise.model.ticket.TicketRequest;
import com.spring.exercise.model.user.AuthRequest;
import com.spring.exercise.repository.OrderRepository;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.utils.ErrorAppMessages;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
public class OrderIntegrationTests extends BaseIntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private JwtUtils jwtUtils;

    private TicketRequest ticketRequest;
    private static final String TICKET_TITLE = "ticket_title";
    private static final BigDecimal TICKET_PRICE = new BigDecimal(13);
    private final static String TICKET_DESCRIPTION = "ticket_description_";

    @BeforeEach
    public void setUp() {
        ticketRequest = new TicketRequest(TICKET_TITLE, TICKET_PRICE, TICKET_DESCRIPTION);
        authRequest = new AuthRequest(USER_NAME, USER_PASSWORD);
    }

    @AfterEach
    public void tearDown() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    public void shouldResponseWith201WhenOrderIsCreated() throws Exception {
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
        result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .content(mapToJson(orderCreateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderStatus").value(expectedOrderStatus))
                .andExpect(jsonPath("$..ticketId").value(ticketId))
                .andExpect(jsonPath("$..price").value(TICKET_PRICE.intValue()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String orderId = new JSONObject(result.getResponse().getContentAsString()).getString("id");
        OrderEntity order = orderRepository.findById(orderId).get();
        TicketEntity ticket = ticketRepository.findById(ticketId).get();
        assertEquals(order.getOrderStatus(), OrderStatus.CREATED);
        assertEquals(order.getTicket().getId(), ticketId);
        assertEquals(order.getTicket().getTitle(), ticket.getTitle());
        assertEquals(order.getTicket().getPrice(), ticket.getPrice());
        assertEquals(ticket.getOrderId(), order.getTicket().getOrderId());
        assertEquals(ticket.getOrderId(), order.getId());
    }

    @Test
    public void shouldResponseWith201WhenOrderIsCreatedAndChangeOrderStatusToCancelled() throws Exception {
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
        String expectedOrderStatus = OrderStatus.CREATED.name();
        result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .content(mapToJson(orderCreateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderStatus").value(expectedOrderStatus))
                .andExpect(jsonPath("$..ticketId").value(ticketId))
                .andExpect(jsonPath("$..price").value(TICKET_PRICE.intValue()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String orderId = new JSONObject(result.getResponse().getContentAsString()).getString("id");
        OrderEntity order = orderRepository.findById(orderId).get();
        TicketEntity ticket = ticketRepository.findById(ticketId).get();
        assertEquals(order.getOrderStatus(), OrderStatus.CREATED);
        assertEquals(order.getTicket().getId(), ticketId);
        assertEquals(order.getTicket().getTitle(), ticket.getTitle());
        assertEquals(order.getTicket().getPrice(), ticket.getPrice());
        assertEquals(ticket.getOrderId(), order.getTicket().getOrderId());
        assertEquals(ticket.getUserId(), order.getTicket().getUserId());
        assertEquals(ticket.getOrderId(), order.getId());
        long waitForOrderExpiration = 20000;
        Thread.sleep(waitForOrderExpiration);
        order = orderRepository.findById(orderId).get();
        ticket = ticketRepository.findById(order.getTicket().getId()).get();
        assertEquals(order.getOrderStatus(), OrderStatus.CANCELLED);
        assertEquals(ticket.getOrderId(), null);

        result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .content(mapToJson(orderCreateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderStatus").value(expectedOrderStatus))
                .andExpect(jsonPath("$..ticketId").value(ticketId))
                .andExpect(jsonPath("$..price").value(TICKET_PRICE.intValue()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        orderId = new JSONObject(result.getResponse().getContentAsString()).getString("id");
        order = orderRepository.findById(orderId).get();
        ticket = ticketRepository.findById(order.getTicket().getId()).get();
        assertEquals(order.getOrderStatus(), OrderStatus.CREATED);
        assertEquals(ticket.getOrderId(), order.getTicket().getOrderId());
        assertEquals(ticket.getUserId(), order.getTicket().getUserId());
    }

    @Test
    public void shouldResponseWith401WhenUserIsNotAuthenticated() throws Exception {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest("ticketId");
        String fakeToken = "fake_token";
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", fakeToken)
                        .content(mapToJson(orderCreateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value(ErrorAppMessages.NOT_AUTHORIZED_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith400WhenTicketIdIsBlank() throws Exception {
        MvcResult ticketOwner = createDefaultUser();
        AuthRequest authRequestUser = new AuthRequest("user_order@mail.com", "pass");
        MvcResult user = createCustomUser(authRequestUser);
        int tokenIndexStart = 7;
        String token = fetchToken(ticketOwner);
        String tokenValue = token.substring(tokenIndexStart);
        String ticketOwnerUserId = jwtUtils.extractId(tokenValue);
        createTicketForUser(ticketOwnerUserId, token, ticketRequest);
        token = fetchToken(user);

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest("");
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(orderCreateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(ErrorAppMessages.BLANK_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith404WhenTicketNotFound() throws Exception {
        MvcResult ticketOwner = createDefaultUser();
        AuthRequest authRequestUser = new AuthRequest("user_order@mail.com", "pass");
        MvcResult user = createCustomUser(authRequestUser);
        int tokenIndexStart = 7;
        String token = fetchToken(ticketOwner);
        String tokenValue = token.substring(tokenIndexStart);
        String ticketOwnerUserId = jwtUtils.extractId(tokenValue);
        createTicketForUser(ticketOwnerUserId, token, ticketRequest);
        token = fetchToken(user);
        String fakeTicketId = "fake_ticket_id";
        String expectedError = "Ticket with id " + fakeTicketId + " was not found";

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(fakeTicketId);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(orderCreateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value(expectedError))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith400WhenTicketIsBooked() throws Exception {
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
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(orderCreateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        token = fetchToken(secondUser);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(orderCreateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(ErrorAppMessages.TICKET_ALREADY_BOOKED_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith400WhenBookOwnTicket() throws Exception {
        MvcResult ticketOwner = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(ticketOwner);
        String tokenValue = token.substring(tokenIndexStart);
        String ticketOwnerUserId = jwtUtils.extractId(tokenValue);
        MvcResult ticketCreateResult = createTicketForUser(ticketOwnerUserId, token, ticketRequest);
        JSONObject jsonObj = new JSONObject(ticketCreateResult.getResponse().getContentAsString());
        String ticketId = jsonObj.getString("id");
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(ticketId);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapToJson(orderCreateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(ErrorAppMessages.OWN_TICKET_PURCHASE_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith200WhenUserRequestHisOrders() throws Exception {
        List<TicketEntity> ticketsList = new ArrayList<>();
        IntStream.range(0, 10)
                .forEach(x -> {
                    ticketsList.add(new TicketEntity(ObjectId.get().toString(),
                            TICKET_TITLE,
                            TICKET_PRICE,
                            TICKET_DESCRIPTION + x,
                            ObjectId.get().toString(),
                            ObjectId.get().toString()));
                });
        ticketRepository.saveAll(ticketsList);
        MvcResult orderOwner = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(orderOwner);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        List<OrderEntity> ordersList = new ArrayList<>();
        int start = 0;
        int end = 10;
        IntStream.range(start, end)
                .forEach(x -> {
                    ordersList.add(new OrderEntity(ObjectId.get().toString(),
                            userId,
                            OrderStatus.CREATED,
                            LocalDateTime.now(),
                            ticketsList.get(x)));
                });
        orderRepository.saveAll(ordersList);

        int currentPage = 0;
        int ordersPerPage = 5;
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/orders/")
                        .header("Authorization", token)
                        .requestAttr("page", currentPage)
                        .requestAttr("size", ordersPerPage)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orders", hasSize(ordersPerPage)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value(currentPage))
                .andExpect(jsonPath("$..id", hasSize(ordersPerPage)))
                .andExpect(jsonPath("$..orderStatus", hasSize(ordersPerPage)))
                .andExpect(jsonPath("$..expiration", hasSize(ordersPerPage)))
                .andExpect(jsonPath("$..ticket", hasSize(ordersPerPage)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        Pageable paging = PageRequest.of(currentPage, ordersPerPage);
        List<OrderEntity> ordersFromDb = orderRepository.findAllByUserId(userId, paging).toList();
        assertEquals(ordersFromDb.size(), ordersPerPage);
    }

    @Test
    public void shouldResponseWith200WhenUserRequestHisOrder() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                ObjectId.get().toString());
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult firstUser = createCustomUser(authRequest);
        token = fetchToken(firstUser);
        JSONObject json = new JSONObject(firstUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");
        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CREATED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/orders/" + order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("CREATED"))
                .andExpect(jsonPath("$..ticketId").value(ticket.getId()))
                .andExpect(jsonPath("$..price").value(TICKET_PRICE.intValue()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith401WhenUserGetOrderAndIsNotAuthenticated() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                ObjectId.get().toString());
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult firstUser = createCustomUser(authRequest);
        token = "fake_token";
        JSONObject json = new JSONObject(firstUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");
        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CREATED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/orders/" + order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value(ErrorAppMessages.NOT_AUTHORIZED_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith404WhenUserOrderNotFound() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                ObjectId.get().toString());
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult firstUser = createCustomUser(authRequest);
        token = fetchToken(firstUser);
        JSONObject json = new JSONObject(firstUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");
        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CREATED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);

        String fakeOrderId = "fake_order_id";
        String expectedError = "Order with id " + fakeOrderId + " was not found";
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/orders/" + fakeOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value(expectedError))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith404WhenUserIsNotOrderOwner() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                ObjectId.get().toString());
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult secondUser = createCustomUser(authRequest);
        JSONObject json = new JSONObject(secondUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");

        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CREATED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);

        String expectedError = "Order with id " + order.getId() + " was not found";
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/orders/" + order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value(expectedError))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith404WhenUserIsNotOrderOwnerAndTryCancel() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                ObjectId.get().toString());
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult secondUser = createCustomUser(authRequest);
        JSONObject json = new JSONObject(secondUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");

        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CREATED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);

        String expectedError = "Order with id " + order.getId() + " was not found";
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/orders/cancel/" + order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value(expectedError))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith404WhenUserCancelNotFoundOrder() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                ObjectId.get().toString());
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult firstUser = createCustomUser(authRequest);
        token = fetchToken(firstUser);
        JSONObject json = new JSONObject(firstUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");
        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CREATED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);

        String fakeOrderId = "fake_order_id";
        String expectedError = "Order with id " + fakeOrderId + " was not found";
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/orders/cancel/" + fakeOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].message").value(expectedError))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith401WhenUserCancelOrderAndIsNotAuthenticated() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                ObjectId.get().toString());
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult firstUser = createCustomUser(authRequest);
        token = "fake_token";
        JSONObject json = new JSONObject(firstUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");
        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CREATED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/orders/cancel/" + order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value(ErrorAppMessages.NOT_AUTHORIZED_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith400WhenOrderStatusIsNotCreated() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                ObjectId.get().toString());
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult firstUser = createCustomUser(authRequest);
        token = fetchToken(firstUser);
        JSONObject json = new JSONObject(firstUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");
        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CANCELLED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);

        String expectedError = "Cannot cancel order with id: " + order.getId() + ". Status is " + order.getOrderStatus();
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/orders/cancel/" + order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(expectedError))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith200AndEmptyBodyWhenUserCancelHisOrder() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        TicketEntity ticket = new TicketEntity(ObjectId.get().toString(),
                TICKET_TITLE,
                TICKET_PRICE,
                TICKET_DESCRIPTION,
                userId,
                null);
        ticketRepository.save(ticket);

        AuthRequest authRequest = new AuthRequest("user_order@mail.com", "pass");
        MvcResult firstUser = createCustomUser(authRequest);
        token = fetchToken(firstUser);
        JSONObject json = new JSONObject(firstUser.getResponse().getContentAsString());
        String secondUserId = json.getString("id");
        long expirationSecond = 900;
        LocalDateTime orderExpirationTime = LocalDateTime.now().plusSeconds(expirationSecond);
        OrderEntity order = new OrderEntity(ObjectId.get().toString(),
                secondUserId,
                OrderStatus.CREATED,
                orderExpirationTime,
                ticket);
        orderRepository.save(order);
        ticket.setOrderId(order.getId());
        ticketRepository.save(ticket);
        Optional<OrderEntity> savedOrder = orderRepository.findById(order.getId());
        assertTrue(savedOrder.isPresent());
        assertEquals(ticket.getOrderId(), order.getId());
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/orders/cancel/" + order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        Optional<OrderEntity> emptyOrder = orderRepository.findById(order.getId());
        ticket = ticketRepository.findById(ticket.getId()).get();
        assertTrue(emptyOrder.isEmpty());
        assertEquals(ticket.getOrderId(), null);
    }
}