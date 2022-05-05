package com.spring.exercise.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    private final static String USER_NAME = "marek_test@gmail.com";
    private final static String USER_PASSWORD = "pass";


    private AuthRequest authRequest;

    @BeforeEach
    public void setUp() {
        authRequest = new AuthRequest(USER_NAME, USER_PASSWORD);
    }

    @Test
    public void shouldReturnStatusOkWhenUserCreated() throws Exception
    {
        mockMvc.perform( MockMvcRequestBuilders
                        .post("/users/sign_up")
                        .content(mapToJson(authRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        Optional<UserEntity> user = userRepository.findByUserName(authRequest.getUsername());
        assertTrue(user.isPresent());
    }

    public static String mapToJson(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    @Test
//    void shouldRegisterWhenUsernameDoesNotExists() throws Exception {
//
//
//        mockMvc.perform(post("/forums/{forumId}/register", 42L)
//                        .contentType("application/json")
//                        .param("sendWelcomeMail", "true")
//                        .content(objectMapper.writeValueAsString(user)))
//                .andExpect(status().isOk());
//
//        UserEntity userEntity = userRepository.findByName("Zaphod");
//        assertThat(userEntity.getEmail()).isEqualTo("zaphod@galaxy.net");
//    }

}


