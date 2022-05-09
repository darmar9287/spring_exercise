package com.spring.exercise.service;

import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.controller.model.AuthResponse;
import com.spring.exercise.controller.model.CurrentUserResponse;
import com.spring.exercise.controller.model.UserDTO;
import com.spring.exercise.exceptions.InvalidCredentialsException;
import com.spring.exercise.exceptions.UserAlreadyExistsException;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.security.UserDetailsImpl;
import com.spring.exercise.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private static final int BEARER_SUBSTRING_LENGTH = 7;

    public UserDTO createUser(AuthRequest authRequest) {
        if (userRepository.findByUserName(authRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        final var userEntity = new UserEntity();
        userEntity.setUserName(authRequest.getUsername());
        userEntity.setPassword(bCryptPasswordEncoder.encode(authRequest.getPassword()));
        userRepository.save(userEntity);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        final var jwt = jwtUtils.generateToken(authentication, userEntity.getId());

        return UserDTO.mapFromEntity(userEntity, jwt);
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

    public AuthResponse generateRegisterResponse(UserDTO user) {
        return AuthResponse.mapFromDTO(user);
    }

    @Override
    public UserDetails loadUserByUsername(String userName) {
        Optional<UserEntity> user = userRepository.findByUserName(userName);
        return UserDetailsImpl.build(user);
    }

    public CurrentUserResponse getCurrentUserResponse(String token) {
        Optional<String> parsedJwt = jwtUtils.parseJwt(token);

        String iat = jwtUtils.extractIat(parsedJwt.get());
        String email = jwtUtils.extractUsername(parsedJwt.get());
        String id = jwtUtils.extractId(parsedJwt.get());
        return new CurrentUserResponse(iat, email, id);
    }

    private Optional<UserEntity> getUserFromDB(String username) {
        return userRepository.findByUserName(username);
    }

}
