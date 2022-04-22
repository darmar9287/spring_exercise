package com.spring.exercise.controller;

import com.spring.exercise.controller.model.AuthRequest;
import com.spring.exercise.model.UserEntity;
import com.spring.exercise.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class AuthenticationController {

    @Autowired
    private UserServiceImpl userServiceImpl;


    @PostMapping(value = "/sign_up", produces = "application/json;charset=UTF-8")
    private ResponseEntity<?> register(@Valid @RequestBody AuthRequest authRequest, Errors errors) {
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();
        userServiceImpl.checkIfCredentialsAreCorrect(errors);

        UserEntity result = new UserEntity(username, password);
        userServiceImpl.createUser(result, errors);

        UserEntity user = new UserEntity(result.getId(),username, password);
        String jwt = userServiceImpl.createRegisterJwt(user);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Authorization", "Bearer " + jwt);

        Map<String, Object> responseMap = userServiceImpl.generateResponse(result);
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(responseMap);
    }

    @PostMapping("/sign_in")
    private ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest, Errors errors) {
        userServiceImpl.checkIfCredentialsAreCorrect(errors);
        String jwt = userServiceImpl.createLoginJwt(authRequest);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Authorization", "Bearer " + jwt);

        return ResponseEntity.ok().headers(responseHeaders).build();
    }
}
