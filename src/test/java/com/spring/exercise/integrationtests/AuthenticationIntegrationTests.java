package com.spring.exercise.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;

    private final static String USER_NAME = "marek_test@gmail.com";
    private final static String USER_PASSWORD = "pass";
    private AuthRequest authRequest;

    @BeforeEach
    public void setUp() {
        authRequest = new AuthRequest(USER_NAME, USER_PASSWORD);
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void shouldResponseWith200WhenUserCreated() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
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

        Optional<UserEntity> user = userRepository.findByUserName(authRequest.getUsername());
        assertTrue(user.isPresent());
        assertEquals(authRequest.getUsername(), user.get().getUserName());
        assertTrue(passwordEncoder.matches(USER_PASSWORD, user.get().getPassword()));
        int tokenIndexStart = 7;
        String token = result.getResponse().getHeader("Authorization").substring(tokenIndexStart);
        String extractedUserId = jwtUtils.extractId(token);
        String extractedUsername = jwtUtils.extractUsername(token);

        assertEquals(user.get().getUserName(),extractedUsername);
        assertEquals(user.get().getId(), extractedUserId);
    }

    @Test
    public void shouldResponseWith401IfUserExists() throws Exception {
        userService.createUser(authRequest);

        String expectedErrorMessage = "Email in use";
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(authRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(expectedErrorMessage));
    }

    @Test
    public void shouldResponseWith400IfEmailIsNotWellFormed() throws Exception {
        AuthRequest malformedRequest = new AuthRequest();
        malformedRequest.setUsername("marek_testgmail.com");
        malformedRequest.setPassword("pass");
        String expectedErrorMessage = "Must be a well-formed email address";

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(expectedErrorMessage));
    }

    @Test
    public void shouldResponseWith400IfPasswordDoesNotMetRequirements() throws Exception {
        AuthRequest malformedRequest = new AuthRequest();
        malformedRequest.setUsername("marek_test@gmail.com");
        malformedRequest.setPassword("pas");

        String expectedErrorMessage = "Size must be between 4 and 20";
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(expectedErrorMessage));
    }

    private static String mapToJson(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


