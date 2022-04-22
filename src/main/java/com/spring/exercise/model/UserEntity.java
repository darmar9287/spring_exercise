package com.spring.exercise.model;

import com.spring.exercise.utils.AppMessages;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    @Email(message = AppMessages.EMAIL_FORMAT_ERROR)
    private String userName;
    @NonNull
    @Size(min = 4, max = 20, message = AppMessages.PASSWORD_SIZE_ERROR)
    private String password;

    public UserEntity(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}