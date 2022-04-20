package com.spring.exercise.Payload.Request;

import com.spring.exercise.utils.AppStringContainer;
import com.spring.exercise.utils.UniqueMail;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
@Setter
public class AuthRequest {
    @Id
    private ObjectId id;

    @Indexed(unique = true)
    @Email(message = AppStringContainer.EMAIL_FORMAT_ERROR)
    @UniqueMail
    private String username;
    @NonNull
    @Size(min = 4, max = 20, message = AppStringContainer.PASSWORD_SIZE_ERROR)
    private String password;
}
