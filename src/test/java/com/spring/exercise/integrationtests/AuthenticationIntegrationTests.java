package com.spring.exercise.integrationtests;

import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.controller.model.UserDTO;
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
import org.springframework.util.StringUtils;

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
class AuthenticationIntegrationTests extends BaseIntegrationTests{
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
    private final static String EMAIL_EXISTS_ERROR = "Email in use";
    private final static String EMAIL_WRONG_FORMAT_ERROR = "Must be a well-formed email address";
    private final static String PASSWORD_SIZE_ERROR = "Size must be between 4 and 20";
    private final static String INCORRECT_CREDENTIALS_ERROR = "Invalid login credentials";
    private final static String NOT_AUTHORIZED_ERROR = "Not authorized";
    private final static int BEARER_SUBSTRING_LENGTH = 7;


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
        MvcResult result = createDefaultUser();
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

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(authRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(EMAIL_EXISTS_ERROR));
    }

    @Test
    public void shouldResponseWith400IfEmailIsNotWellFormed() throws Exception {
        AuthRequest malformedRequest = new AuthRequest();
        malformedRequest.setUsername("marek_testgmail.com");
        malformedRequest.setPassword("pass");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(EMAIL_WRONG_FORMAT_ERROR));
    }

    @Test
    public void shouldResponseWith400IfPasswordDoesNotMetRequirements() throws Exception {
        AuthRequest malformedRequest = new AuthRequest();
        malformedRequest.setUsername("marek_test@gmail.com");
        malformedRequest.setPassword("pas");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(PASSWORD_SIZE_ERROR));
    }

    @Test
    public void shouldLoginWhenCredentialsAreCorrect() throws Exception {
        UserDTO user = userService.createUser(authRequest);

        MvcResult result = userLoginAction(authRequest);

        int tokenIndexStart = 7;
        String token = result.getResponse().getHeader("Authorization").substring(tokenIndexStart);
        String extractedUserId = jwtUtils.extractId(token);
        String extractedUsername = jwtUtils.extractUsername(token);

        assertEquals(user.getUserName(), extractedUsername);
        assertEquals(user.getId(), extractedUserId);
    }

    @Test
    public void shouldNotAcceptLoginRequestWhenCredentialsAreIncorrect() throws Exception {
        userService.createUser(authRequest);
        AuthRequest malformedLoginRequest = new AuthRequest("marek1@gmail.com", "sapp");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_in")
                        .content(mapToJson(malformedLoginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(INCORRECT_CREDENTIALS_ERROR));
    }

    @Test
    public void shouldNotAcceptLoginRequestWhenEmailIsNotWellFormed() throws Exception {
        userService.createUser(authRequest);
        AuthRequest malformedLoginRequest = new AuthRequest("marekgmail.com", "pass");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_in")
                        .content(mapToJson(malformedLoginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(EMAIL_WRONG_FORMAT_ERROR))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldNotAcceptLoginRequestWhenPasswordSizeDoesNotMatch() throws Exception {
        userService.createUser(authRequest);
        AuthRequest malformedLoginRequest = new AuthRequest("marek@gmail.com", "pss");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_in")
                        .content(mapToJson(malformedLoginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(PASSWORD_SIZE_ERROR))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldReturnCurrentUserIfTokenIsCorrect() throws Exception{
        MvcResult resultUser = createDefaultUser();

        String token = resultUser.getResponse().getHeader("Authorization").toString();
        Optional<UserEntity> user = userRepository.findByUserName(authRequest.getUsername());
        assertTrue(user.isPresent());
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/currentuser")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String parsedJwt = null;
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            parsedJwt = token.substring(BEARER_SUBSTRING_LENGTH);
        }
        String extractedUserId = jwtUtils.extractId(parsedJwt);
        String extractedUsername = jwtUtils.extractUsername(parsedJwt);
        assertEquals(user.get().getUserName(), extractedUsername);
        assertEquals(user.get().getId(), extractedUserId);
    }

    @Test
    public void shouldResponseWith401WhenUserIsNotAuthorized() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/currentuser")
                        .header("Authorization", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(NOT_AUTHORIZED_ERROR));

    }
}