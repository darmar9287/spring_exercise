package com.spring.exercise.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUserResponse {
    private String iat;
    private String email;
    private String id;
}
