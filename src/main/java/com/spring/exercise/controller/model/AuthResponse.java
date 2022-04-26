package com.spring.exercise.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String id;
    private String email;

    public static AuthResponse mapFromDTO(UserDTO userDTO) {
        return new AuthResponse(userDTO.getId(), userDTO.getUserName());
    }
}
