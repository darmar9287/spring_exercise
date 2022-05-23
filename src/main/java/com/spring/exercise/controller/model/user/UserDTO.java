package com.spring.exercise.controller.model.user;

import com.spring.exercise.model.UserEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserDTO {

    private final String id;
    private final String userName;
    private final String jwt;

    public static UserDTO mapFromEntity(UserEntity userEntity, String jwt) {
        return new UserDTO(userEntity.getId(), userEntity.getUserName(), jwt);
    }
}
