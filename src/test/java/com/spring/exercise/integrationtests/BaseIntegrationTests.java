package com.spring.exercise.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring.exercise.model.ticket.TicketRequest;
import com.spring.exercise.model.user.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class BaseIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    protected RegistrationRequest registrationRequest;
    protected final static String USER_NAME = "marek_test@gmail.com";
    protected final static String USER_PASSWORD = "pass";
    protected final static String TICKET_DESCRIPTION = "fake_description_";
    protected static LocalDate DATE_OF_BIRTH = LocalDate.of(1987, 1, 8);

    protected String fetchToken(MvcResult resultUser) {
        return resultUser.getResponse().getHeader("Authorization");
    }

    protected MvcResult userLoginAction(RegistrationRequest registrationRequest) throws Exception {
       return mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_in")
                        .content(mapToJson(registrationRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    protected MvcResult createDefaultUser() throws Exception{
        registrationRequest.setUsername("marek_test@gmail.com");
        registrationRequest.setPassword("pass");
        return mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(registrationRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(header().stringValues("Authorization", hasItems(containsString("Bearer"))))
                .andReturn();
    }

    protected MvcResult createCustomUser(RegistrationRequest registrationRequest) throws Exception{
        return mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(registrationRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(header().stringValues("Authorization", hasItems(containsString("Bearer"))))
                .andReturn();
    }

    protected MvcResult createTicketForUser(String userId, String token, TicketRequest ticketRequest) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .post("/tickets/create")
                        .content(mapToJson(ticketRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(ticketRequest.getTitle()))
                .andExpect(jsonPath("$.price").value(ticketRequest.getPrice()))
                .andExpect(jsonPath("$.userId").value(userId))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    protected static String mapToJson(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}