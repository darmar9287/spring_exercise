package com.spring.exercise.model;

import com.spring.exercise.utils.AppStringContainer;
import com.spring.exercise.utils.UniqueMail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserModel {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    @Email(message = AppStringContainer.EMAIL_FORMAT_ERROR)
    @UniqueMail
    private String userName;
    @NonNull
    @Size(min = 4, max = 20, message = AppStringContainer.PASSWORD_SIZE_ERROR)
    private String password;

    public UserModel(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

}