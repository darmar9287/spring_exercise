package com.spring.exercise.controller.model;

import com.spring.exercise.model.UserEntity;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AuthResponse {

    private String field;
    private String message;

    public static Map<String, Object> generateResponse(UserEntity user) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("email", user.getUserName());
        responseMap.put("id", user.getId());

        return responseMap;
    }
}
