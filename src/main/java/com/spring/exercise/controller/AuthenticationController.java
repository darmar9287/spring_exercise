package com.spring.exercise.controller;

import com.spring.exercise.Payload.Request.AuthRequest;
import com.spring.exercise.exceptions.InvalidLoginCredentialsException;
import com.spring.exercise.model.UserModel;
import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/users")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping(value = "/sign_up", produces = "application/json;charset=UTF-8")
    private ResponseEntity<?> register(@Valid @RequestBody AuthRequest authRequest, Errors errors) {
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();

        userServiceImpl.validateUserCredentials(true, username, password, errors);
        UserModel user = new UserModel(authRequest.getUsername(), authRequest.getPassword());
        UserModel result = userServiceImpl.createUser(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        String jwt = jwtUtils.generateToken(authentication);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Authorization", "Bearer " + jwt);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("email", result.getUserName());
        responseMap.put("id", result.getId());
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(responseMap);
    }

    @PostMapping("/sign_in")
    private ResponseEntity<?> login(@RequestBody AuthRequest authRequest, Errors errors) {

        String username = authRequest.getUsername();
        String password = authRequest.getPassword();
        userServiceImpl.validateUserCredentials(false, username, password, errors);

        Authentication authentication = null;
        HttpHeaders responseHeaders = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            String jwt = jwtUtils.generateToken(authentication);
            responseHeaders = new HttpHeaders();
            responseHeaders.set("Authorization", "Bearer " + jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            throw new InvalidLoginCredentialsException();
        }

        return ResponseEntity.ok().headers(responseHeaders).body("");
    }
}
