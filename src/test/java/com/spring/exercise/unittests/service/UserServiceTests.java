package com.spring.exercise.unittests.service;

import com.spring.exercise.model.user.AuthRequest;
import com.spring.exercise.model.user.UserDTO;
import com.spring.exercise.exceptions.UserAlreadyExistsException;
import com.spring.exercise.entity.UserEntity;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private Authentication authentication;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserEntity user;

    private AuthRequest request;

    private final static String USER_NAME = "marek_test@gmail.com";
    private final static String USER_PASS = "pass";

    public UserServiceTests() { }

    @BeforeEach
    public void setUp() {
        user = new UserEntity();
        user.setId(ObjectId.get().toString());
        user.setUserName(USER_NAME);
        user.setPassword(USER_PASS);

        request = new AuthRequest(USER_NAME, USER_PASS);
    }

    @Test
    void testFindByNameShouldReturnUser() {
        //when
        when(userRepository.findByUserName((user.getUserName()))).thenReturn(Optional.of(user));
        UserDetails fetchedUser = userService.loadUserByUsername(user.getUserName());
        //then
        assertEquals(fetchedUser.getUsername(), user.getUserName());
    }

    @Test
    void shouldSuccessWhenCreateUserWithCorrectCredentials() {
        //when
        when(userRepository.save(any())).thenReturn(user);
        when(passwordEncoder.encode(USER_PASS)).thenReturn(USER_PASS);
        //then
        UserDTO createdUser = userService.createUser(request);
        assertEquals(createdUser.getUserName(), USER_NAME);
    }

    @Test
    public void shouldThrowExceptionWhenUsernameExists() {
        //when
        when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.of(user));
        //then
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    public void shouldReturnJwtIfAuthenticationPass() {
        //given
        String exampleJwt = "3x4mpl3jwt";
        //when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUserName(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(any(), any())).thenReturn(exampleJwt);
        //then
        String jwt = userService.createLoginJwt(request);
        assertTrue(!jwt.isEmpty());
    }

    @Test
    public void shouldReturnUserDetailsWhenUsernameProvided() {
        //when
        when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.of(user));
        //then
        UserDetails userDetails = userService.loadUserByUsername(user.getUserName());
        assertEquals(userDetails.getUsername(), USER_NAME);
        assertEquals(userDetails.getPassword(), USER_PASS);
    }
}