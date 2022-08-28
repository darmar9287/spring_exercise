package com.spring.exercise.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

    private String id;
    private String email;

    public static RegisterResponse mapFromDTO(UserDTO userDTO) {
        return new RegisterResponse(userDTO.getId(), userDTO.getUserName());
    }
}
