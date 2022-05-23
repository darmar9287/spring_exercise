package com.spring.exercise.controller;

import com.spring.exercise.controller.model.user.AuthRequest;
import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.RequestBodyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class AuthenticationController {

    private final UserServiceImpl userServiceImpl;

    @PostMapping(value = "/sign_up", produces = "application/json;charset=UTF-8")
    private ResponseEntity<?> register(@Valid @RequestBody AuthRequest authRequest, Errors errors) {
        RequestBodyValidator.check(errors);
        var result = userServiceImpl.createUser(authRequest);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Authorization", "Bearer " + result.getJwt());

        final var response = userServiceImpl.generateRegisterResponse(result);
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(response);
    }

    @PostMapping("/sign_in")
    private ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest, Errors errors) {
        RequestBodyValidator.check(errors);
        String jwt = userServiceImpl.createLoginJwt(authRequest);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Authorization", "Bearer " + jwt);

        return ResponseEntity.ok().headers(responseHeaders).build();
    }

    @GetMapping("/currentuser")
    private ResponseEntity<?> getCurrentUser(@RequestHeader(name = "Authorization") String token) {
        final var response = userServiceImpl.getCurrentUserResponse(token);
        return ResponseEntity.ok().body(Map.of("currentUser:", response));
    }
}