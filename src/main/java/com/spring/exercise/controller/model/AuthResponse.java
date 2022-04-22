package com.spring.exercise.controller.model;

import com.spring.exercise.model.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AuthResponse {

    public static Map<String, Object> generateResponse(UserEntity user) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("email", user.getUserName());
        responseMap.put("id", user.getId());

        return responseMap;
    }
}
