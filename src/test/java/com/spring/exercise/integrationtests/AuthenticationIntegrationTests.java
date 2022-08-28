package com.spring.exercise.integrationtests;

import com.spring.exercise.entity.UserEntity;
import com.spring.exercise.model.user.LoginRequest;
import com.spring.exercise.model.user.RegistrationRequest;
import com.spring.exercise.model.user.UserDTO;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.AppMessages;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "/test.properties")
class AuthenticationIntegrationTests extends BaseIntegrationTests {
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

    @BeforeEach
    public void setUp() {
        loginRequest = new LoginRequest(USER_NAME, USER_PASSWORD);
        registrationRequest = new RegistrationRequest(USER_NAME, USER_PASSWORD, DATE_OF_BIRTH);
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void shouldResponseWith200WhenUserCreated() throws Exception {
        MvcResult result = createDefaultUser();
        Optional<UserEntity> user = userRepository.findByUserName(registrationRequest.getUsername());
        assertTrue(user.isPresent());
        assertEquals(registrationRequest.getUsername(), user.get().getUserName());
        assertTrue(passwordEncoder.matches(USER_PASSWORD, user.get().getPassword()));
        int tokenIndexStart = 7;
        String token = result.getResponse().getHeader("Authorization").substring(tokenIndexStart);
        String extractedUserId = jwtUtils.extractId(token);
        String extractedUsername = jwtUtils.extractUsername(token);

        assertEquals(user.get().getUserName(), extractedUsername);
        assertEquals(user.get().getId(), extractedUserId);
        assertEquals(user.get().getDateOfBirth(), DATE_OF_BIRTH);
    }

    @Test
    public void shouldResponseWith401IfUserExists() throws Exception {
        userService.createUser(registrationRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(registrationRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(AppMessages.EMAIL_EXISTS_ERROR));
    }

    @Test
    public void shouldResponseWith400IfEmailIsNotWellFormed() throws Exception {
        RegistrationRequest malformedRequest = new RegistrationRequest();
        malformedRequest.setUsername("marek_testgmail.com");
        malformedRequest.setPassword("pass");
        malformedRequest.setDateOfBirth(DATE_OF_BIRTH);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(AppMessages.EMAIL_FORMAT_ERROR));
    }

    @Test
    public void shouldResponseWith400IfPasswordDoesNotMetRequirements() throws Exception {
        RegistrationRequest malformedRequest = new RegistrationRequest();
        malformedRequest.setUsername("marek_test@gmail.com");
        malformedRequest.setPassword("pas");
        malformedRequest.setDateOfBirth(DATE_OF_BIRTH);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(AppMessages.PASSWORD_SIZE_ERROR));
    }

    @Test
    public void shouldResponseWith400IfAgeDoesNotMetRequirements() throws Exception {
        RegistrationRequest malformedRequest = new RegistrationRequest();
        LocalDate age = LocalDate.now();
        malformedRequest.setUsername("marek_test@gmail.com");
        malformedRequest.setPassword("pass");
        malformedRequest.setDateOfBirth(age);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(malformedRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(AppMessages.AGE_REQUIREMENT_ERROR));
    }

    @Test
    public void shouldLoginWhenCredentialsAreCorrect() throws Exception {
        UserDTO user = userService.createUser(registrationRequest);
        MvcResult result = userLoginAction(loginRequest);

        int tokenIndexStart = 7;
        String token = result.getResponse().getHeader("Authorization").substring(tokenIndexStart);
        String extractedUserId = jwtUtils.extractId(token);
        String extractedUsername = jwtUtils.extractUsername(token);

        assertEquals(user.getUserName(), extractedUsername);
        assertEquals(user.getId(), extractedUserId);
    }

    @Test
    public void shouldNotAcceptLoginRequestWhenCredentialsAreIncorrect() throws Exception {
        userService.createUser(registrationRequest);
        LoginRequest malformedLoginRequest = new LoginRequest("marek1@gmail.com", "sapp");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_in")
                        .content(mapToJson(malformedLoginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(AppMessages.INCORRECT_CREDENTIALS_ERROR));
    }

    @Test
    public void shouldNotAcceptLoginRequestWhenEmailIsNotWellFormed() throws Exception {
        userService.createUser(registrationRequest);
        LoginRequest malformedLoginRequest = new LoginRequest("marekgmail.com", "pass");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_in")
                        .content(mapToJson(malformedLoginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(AppMessages.EMAIL_FORMAT_ERROR))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldNotAcceptLoginRequestWhenPasswordSizeDoesNotMatch() throws Exception {
        userService.createUser(registrationRequest);
        LoginRequest malformedLoginRequest = new LoginRequest("marek@gmail.com", "pss");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/sign_in")
                        .content(mapToJson(malformedLoginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(AppMessages.PASSWORD_SIZE_ERROR))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldReturnCurrentUserIfTokenIsCorrect() throws Exception {
        MvcResult resultUser = createDefaultUser();

        String token = resultUser.getResponse().getHeader("Authorization").toString();
        Optional<UserEntity> user = userRepository.findByUserName(loginRequest.getUsername());

        assertTrue(user.isPresent());
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/currentuser")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$..iat").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$..email").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$..id").exists())
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        String parsedJwt = jwtUtils.parseJwt(token).get();
        String extractedUserId = jwtUtils.extractId(parsedJwt);
        String extractedUsername = jwtUtils.extractUsername(parsedJwt);
        assertEquals(user.get().getUserName(), extractedUsername);
        assertEquals(user.get().getId(), extractedUserId);
    }

    @Test
    public void shouldResponseWith401WhenUserIsNotAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/currentuser")
                        .header("Authorization", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value(AppMessages.NOT_AUTHORIZED_ERROR));
    }
}