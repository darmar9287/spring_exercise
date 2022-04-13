package com.spring.exercise.controller;

import com.spring.exercise.model.AuthenticationRequest;
import com.spring.exercise.model.AuthenticationResponse;
import com.spring.exercise.model.UserModel;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.service.UserService;
import com.spring.exercise.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users")
public class AuthenticationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/sign_in")
    private ResponseEntity<?> register(@RequestBody AuthenticationRequest authenticationRequest) {
        String userMail = authenticationRequest.getMail();
        String password = authenticationRequest.getPassword();

        UserModel userModel = new UserModel();
        userModel.setEmail(userMail);
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
        String userName = authenticationRequest.getMail();
        String password = authenticationRequest.getPassword();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        } catch (Exception e) {
            return ResponseEntity.ok(new AuthenticationResponse("Error occurred while authenticating user"));
        }

        UserDetails loadedUser = userService.loadUserByUsername(userName);

        String generatedToken = jwtUtils.generateToken(loadedUser);

        return ResponseEntity.ok(new AuthenticationResponse(generatedToken));

    }

}
