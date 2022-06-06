package com.spring.exercise.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.exercise.model.ticket.TicketRequest;
import com.spring.exercise.model.user.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class BaseIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    protected AuthRequest authRequest;
    protected final static String USER_NAME = "marek_test@gmail.com";
    protected final static String USER_PASSWORD = "pass";

    protected String fetchToken(MvcResult resultUser) {
        return resultUser.getResponse().getHeader("Authorization");
    }

    protected MvcResult userLoginAction(AuthRequest authRequest) throws Exception {
       return mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_in")
                        .content(mapToJson(authRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    protected MvcResult createDefaultUser() throws Exception{
        authRequest.setUsername("marek_test@gmail.com");
        authRequest.setPassword("pass");
        return mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(authRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(header().stringValues("Authorization", hasItems(containsString("Bearer"))))
                .andReturn();
    }

    protected MvcResult createCustomUser(AuthRequest authRequest) throws Exception{
        return mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(authRequest))
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
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}