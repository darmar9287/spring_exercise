package com.spring.exercise.controller;

import com.spring.exercise.exceptions.CustomExceptions;
import com.spring.exercise.model.AuthenticationRequest;
import com.spring.exercise.model.AuthenticationResponse;
import com.spring.exercise.model.UserModel;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.service.UserService;
import com.spring.exercise.utils.ErrorPojo;
import com.spring.exercise.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    private ResponseEntity<?> register(@Valid @RequestBody UserModel authenticationRequest, Errors errors) {
        String userMail = authenticationRequest.getMail();
        String password = authenticationRequest.getPassword();
        userService.validateUserCredentials(userMail, password, errors);
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
