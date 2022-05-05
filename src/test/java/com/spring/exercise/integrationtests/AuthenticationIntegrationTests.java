package com.spring.exercise.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserServiceImpl userService;

    private final static String USER_NAME = "marek_test@gmail.com";
    private final static String USER_PASSWORD = "pass";

    private AuthRequest authRequest;

    @BeforeEach
    public void setUp() {
        authRequest = new AuthRequest(USER_NAME, USER_PASSWORD);
    }

    @Test
    public void shouldReturnStatusOkWhenUserCreated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(authRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(header().stringValues("Authorization", hasItems(containsString("Bearer"))));

        Optional<UserEntity> user = userRepository.findByUserName(authRequest.getUsername());
        assertTrue(user.isPresent());
        userRepository.delete(user.get());
    }

    @Test
    public void shouldResponseWith401IfUserExists() throws Exception {
        userService.createUser(authRequest);

        String errorResponse = "{\"errors\":[{\"message\":\"Email in use\"}]}";
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(authRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists())
                .andExpect(content().json(errorResponse));
    }

    @Test
    public void shouldResponseWith400IfEmailIsNotWellFormed() throws Exception {
        AuthRequest malformedRequest = new AuthRequest();
        malformedRequest.setUsername("marek_testgmail.com");
        malformedRequest.setPassword("pass");

        String errorResponse = "{\"errors\":[{\"field\":\"username\",\"message\":\"Must be a well-formed email address\"}]}";
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists())
                .andExpect(content().json(errorResponse));
    }

    @Test
    public void shouldResponseWith400IfPasswordDoesNotMetRequirements() throws Exception {
        AuthRequest malformedRequest = new AuthRequest();
        malformedRequest.setUsername("marek_test@gmail.com");
        malformedRequest.setPassword("pas");

        String errorResponse = "{\"errors\":[{\"field\":\"password\",\"message\":\"Size must be between 4 and 20\"}]}";
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists())
                .andExpect(content().json(errorResponse));
    }

    private static String mapToJson(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


