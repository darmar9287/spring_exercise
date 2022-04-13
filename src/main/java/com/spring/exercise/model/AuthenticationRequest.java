package com.spring.exercise.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationRequest {

    String mail;
    String password;

    public AuthenticationRequest(String mail, String password) {
        this.mail = mail;
        this.password = password;
    }
}
