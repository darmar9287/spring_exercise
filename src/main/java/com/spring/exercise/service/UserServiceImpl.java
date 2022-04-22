package com.spring.exercise.service;

import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.controller.model.AuthResponse;
import com.spring.exercise.exceptions.InvalidCredentialsException;
import com.spring.exercise.exceptions.InvalidUserInputException;
import com.spring.exercise.exceptions.UserAlreadyExistsException;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.security.UserDetailsImpl;
import com.spring.exercise.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public UserEntity createUser(UserEntity user, Errors errors) {
        if (userRepository.findByUserName(user.getUserName()).isPresent()) {
            throw new UserAlreadyExistsException(errors);
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    public String createRegisterJwt(UserEntity user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
        return jwtUtils.generateToken(authentication, user.getId());
    }

    public String createLoginJwt(AuthRequest authRequest) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
        Optional<UserEntity> user = getUserFromDB(authRequest.getUsername());
        return jwtUtils.generateToken(authentication, user.get().getId());
    }

    public Map<String, Object> generateResponse(UserEntity user) {
        return AuthResponse.generateResponse(user);
    }

    public void checkIfCredentialsAreCorrect(Errors errors) {
        if (errors.hasErrors()) {
            throw new InvalidUserInputException(400, errors);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String userName) {
        Optional<UserEntity> user = userRepository.findByUserName(userName);
        return UserDetailsImpl.build(user);
    }

    public Optional<UserEntity> getUserFromDB(String username) {
        return userRepository.findByUserName(username);
    }

}
