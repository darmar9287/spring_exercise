package com.spring.exercise.integrationtests;

import com.jayway.jsonpath.JsonPath;
import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.controller.model.TicketRequest;
import com.spring.exercise.model.TicketEntity;
import com.spring.exercise.repository.TicketRepository;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.utils.JwtUtils;
import io.jsonwebtoken.Jwt;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.thymeleaf.spring5.expression.Mvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TicketIntegrationTests extends BaseIntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;

    private TicketRequest ticketRequest;
    private static final String TICKET_TITLE = "ticket_title";
    private static final String TICKET_TITLE_BLANK_ERROR = "must not be blank";
    private static final String TICKET_PRICE_INCORRECT_ERROR = "must be greater than or equal to 0.5";
    private static final String USER_NOT_AUTHORIZED_TO_UPDATE = "Not authorized";

    private static final BigDecimal TICKET_PRICE = new BigDecimal(13);

    @BeforeEach
    public void setUp() {
        ticketRequest = new TicketRequest(TICKET_TITLE, TICKET_PRICE);
        authRequest = new AuthRequest(USER_NAME, USER_PASSWORD);
    }

    @AfterEach
    public void tearDown() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldResponseWith201WhenTicketCreated() throws Exception {
        MvcResult user = createDefaultUser();
        int tokenIndexStart = 7;
        String token = fetchToken(user);
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        MvcResult result = createTicketForUser(userId, token);
        JSONObject jsonObj = new JSONObject(result.getResponse().getContentAsString());
        String ticketId = jsonObj.getString("id");

        TicketEntity ticket = ticketRepository.findById(ticketId).get();
        assertEquals(ticketRequest.getTitle(), ticket.getTitle());
        assertEquals(ticketRequest.getPrice(), ticket.getPrice());
        assertEquals(userId, ticket.getUserId());
    }

    @Test
    public void shouldResponseWith401WhenUserIsNotAuthenticated() throws Exception {
        String fakeToken = "fake_token";
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/tickets/create")
                        .content(mapToJson(ticketRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", fakeToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(NOT_AUTHORIZED_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldResponseWith400WhenTitleIsBlank() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        TicketRequest incorrectTicketRequest = new TicketRequest("", new BigDecimal(13));
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/tickets/create")
                        .content(mapToJson(incorrectTicketRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(TICKET_TITLE_BLANK_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldNotCreateTicketAndResponseWith400WhenPriceIsIncorrect() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        TicketRequest incorrectTicketRequest = new TicketRequest("title", new BigDecimal(0));
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/tickets/create")
                        .content(mapToJson(incorrectTicketRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(TICKET_PRICE_INCORRECT_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldUpdateTicketAndResponseWith201WhenRequestIsCorrect() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        int tokenIndexStart = 7;
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        String ticketCreateResponse = createTicketForUser(userId, token).getResponse().getContentAsString();
        String ticketId = JsonPath.parse(ticketCreateResponse).read("$.id");
        String ticketTitle = "some_title";
        var ticketPrice = new BigDecimal(45);
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tickets/update/" + ticketId)
                        .content(mapToJson(new TicketRequest(ticketTitle, ticketPrice)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(ticketId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(ticketTitle))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(ticketPrice))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(userId))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldNotUpdateTicketAndResponseWith400WhenPriceIsIncorrect() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        int tokenIndexStart = 7;
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        String ticketCreateResponse = createTicketForUser(userId, token).getResponse().getContentAsString();
        String ticketId = JsonPath.parse(ticketCreateResponse).read("$.id");
        TicketRequest incorrectTicketRequest = new TicketRequest("title", new BigDecimal(0));
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tickets/update/" + ticketId)
                        .content(mapToJson(incorrectTicketRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(TICKET_PRICE_INCORRECT_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldNotUpdateTicketAndResponseWith400WhenTitleIsBlank() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        int tokenIndexStart = 7;
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);
        String ticketCreateResponse = createTicketForUser(userId, token).getResponse().getContentAsString();
        String ticketId = JsonPath.parse(ticketCreateResponse).read("$.id");
        TicketRequest incorrectTicketRequest = new TicketRequest("", new BigDecimal(12));
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tickets/update/" + ticketId)
                        .content(mapToJson(incorrectTicketRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(TICKET_TITLE_BLANK_ERROR))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void shouldNotUpdateTicketAndResponseWith401WhenTicketDoesNotBelongToUser() throws Exception {
        MvcResult user = createDefaultUser();
        String token = fetchToken(user);
        int tokenIndexStart = 7;
        String tokenValue = token.substring(tokenIndexStart);
        String userId = jwtUtils.extractId(tokenValue);

        MvcResult fakeUser = createCustomUser(new AuthRequest("fake_user@fake.com", "pass"));
        String fakeUserToken = fetchToken(fakeUser);

        String ticketCreateResponse = createTicketForUser(userId, token).getResponse().getContentAsString();
        String ticketId = JsonPath.parse(ticketCreateResponse).read("$.id");
        TicketRequest incorrectTicketRequest = new TicketRequest("fake_title", new BigDecimal(12));
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tickets/update/" + ticketId)
                        .content(mapToJson(incorrectTicketRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", fakeUserToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(USER_NOT_AUTHORIZED_TO_UPDATE))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    private String fetchToken(MvcResult resultUser) {
        return resultUser.getResponse().getHeader("Authorization");
    }

    private MvcResult createTicketForUser(String userId, String token) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .post("/tickets/create")
                        .content(mapToJson(ticketRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(ticketRequest.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(ticketRequest.getPrice()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(userId))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }
}