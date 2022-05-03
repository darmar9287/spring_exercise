package com.spring.exercise.unittests;

import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.controller.model.UserDTO;
import com.spring.exercise.exceptions.UserAlreadyExistsException;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.JwtUtils;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    public UserServiceTests() { }

    @BeforeEach
    public void setUp() {
        user = new UserEntity();
        user.setUserName("marek@gmail.com");
        user.setId("1");
        user.setPassword("pass");

        request = new AuthRequest("marek@gmail.com", "pass");
    }

    @Test
    void testFindByNameShouldReturnUser() {
        //when
        when(userRepository.findByUserName((user.getUserName()))).thenReturn(Optional.of(user));
        UserDetails fetchedUser = userService.loadUserByUsername(user.getUserName());
        //then
        assertThat(fetchedUser.getUsername()).isEqualTo(user.getUserName());
    }

    @Test
    void shouldSuccessWhenCreateUserWithCorrectCredentials() {
        //when
        when(userRepository.save(user)).thenReturn(user);
        when(passwordEncoder.encode("pass")).thenReturn("pass");
        //then
        UserDTO createdUser = userService.createUser(request);
        assertThat(createdUser.getUserName()).isEqualTo(user.getUserName());
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
        assertThat(!jwt.isEmpty());
    }

    @Test
    public void shouldReturnUserDetailsWhenUsernameProvided() {
;
        //when
        when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.of(user));
        //then
        UserDetails userDetails = userService.loadUserByUsername(user.getUserName());
        assertThat(userDetails.getUsername().equals("marek@gmail.com"));


    }

}
