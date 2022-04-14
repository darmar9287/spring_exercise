package com.spring.exercise.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AuthenticationResponse {

    public static ResponseEntity<Object> generateResponse(String message, HttpStatus status, UserModel responseObj) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("email: ", responseObj.getMail());
        map.put("id", responseObj.getId().toString());
        map.put("message", message);
        map.put("status", status.value());

        return new ResponseEntity<Object>(map,status);
    }
}
