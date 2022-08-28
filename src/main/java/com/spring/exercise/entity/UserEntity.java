package com.spring.exercise.entity;

import com.spring.exercise.utils.CustomValidators.Age;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private static final int MIN_USER_AGE = 18;
    @Id
    private String id;
    @Indexed(unique = true)
    @Email
    private String userName;
    @NonNull
    @Size(min = 4, max = 20)
    private String password;
    @Age(value = MIN_USER_AGE)
    private LocalDate dateOfBirth;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity user = (UserEntity) o;
        return id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, password);
    }
}