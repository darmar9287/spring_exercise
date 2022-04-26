package com.spring.exercise.controller.model;

import com.spring.exercise.utils.AppMessages;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

    @Email(message = AppMessages.EMAIL_FORMAT_ERROR)
    private String username;
    @NotBlank
    @Size(min = 4, max = 20, message = AppMessages.PASSWORD_SIZE_ERROR)
    private String password;
}
