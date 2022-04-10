package com.spring.exercise.controller;

import com.spring.exercise.model.AuthenticationRequest;
import com.spring.exercise.model.AuthenticationResponse;
import com.spring.exercise.model.UserModel;
import com.spring.exercise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/sign_in")
    private ResponseEntity<?> register(@RequestBody AuthenticationRequest authenticationRequest) {
        String userName = authenticationRequest.getUserName();
        String password = authenticationRequest.getPassword();

        UserModel userModel = new UserModel();
        userModel.setUsername(userName);
        userModel.setPassword(password);

        try {
            userRepository.save(userModel);
        } catch (Exception e) {
            return ResponseEntity.ok(new AuthenticationResponse("Error occured"));
        }

        return ResponseEntity.ok(new AuthenticationResponse("User created successfully"));
    }

    @PostMapping("/sign_up")
    private ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest) {
        String userName = authenticationRequest.getUserName();
        String password = authenticationRequest.getPassword();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        } catch (Exception e) {
            return ResponseEntity.ok(new AuthenticationResponse("Error occurred while authenticating user"));
        }
        return ResponseEntity.ok(new AuthenticationResponse("User login successfully"));

    }

}
