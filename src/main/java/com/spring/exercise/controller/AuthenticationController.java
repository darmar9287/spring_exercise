package com.spring.exercise.controller;

import com.spring.exercise.model.AuthenticationRequest;
import com.spring.exercise.model.AuthenticationResponse;
import com.spring.exercise.model.UserModel;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.service.UserService;
import com.spring.exercise.utils.ErrorPojo;
import com.spring.exercise.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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

    @PostMapping(value = "/sign_in", produces = "application/json;charset=UTF-8")
    private ResponseEntity<?> register(@RequestBody AuthenticationRequest authenticationRequest) {
        String userMail = authenticationRequest.getMail();
        String password = authenticationRequest.getPassword();
        Map<String, String> errorMessages = new HashMap<>();
        Map<String, List<ErrorPojo>> responseMap = new HashMap<>();
        userService.validateUserCredentials(userMail, password, errorMessages);

        if (errorMessages.size() > 0) {
            List<ErrorPojo> errors = new ArrayList<>();

            for (Map.Entry<String, String> entry : errorMessages.entrySet()) {
                errors.add(new ErrorPojo(entry.getKey(), entry.getValue()));
            }
            responseMap.put("errors", errors);

            return ResponseEntity.unprocessableEntity().body(responseMap);
        }

        //return new ResponseEntity<>(responseMap, HttpStatus.HTTP_VERSION_NOT_SUPPORTED);
        //return ResponseEntity.unprocessableEntity().body(errorMessages);


        UserModel result = userService.createUser(userMail, password);

        return AuthenticationResponse.generateResponse("Successfully added data!",HttpStatus.OK,result);

}

    @PostMapping("/sign_up")
    private ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest) {
        String userName = authenticationRequest.getMail();
        String password = authenticationRequest.getPassword();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        } catch (Exception e) {
            //return ResponseEntity.ok(new AuthenticationResponse("Error occurred while authenticating user"));
        }
        UserDetails loadedUser = userService.loadUserByUsername(userName);
        String generatedToken = jwtUtils.generateToken(loadedUser);

        return ResponseEntity.ok().body("");

    }

}
