package com.spring.exercise.model.user;

import com.spring.exercise.utils.ErrorAppMessages;
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
    @NotBlank
    @Email(message = ErrorAppMessages.EMAIL_FORMAT_ERROR)
    private String username;
    @NotBlank
    @Size(min = 4, max = 20, message = ErrorAppMessages.PASSWORD_SIZE_ERROR)
    private String password;
}