package com.spring.exercise.model;

import com.spring.exercise.utils.AppStringContainer;
import com.spring.exercise.utils.UniqueMail;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Document(collection = "users")
@Getter @Setter
public class UserModel {

  @Id
  private ObjectId id;

  @Indexed(unique = true)
  @Email(message = AppStringContainer.EMAIL_FORMAT_ERROR)
  @UniqueMail
  private String mail;
  @NonNull
  @Size(min = 4, max = 20, message = AppStringContainer.PASSWORD_SIZE_ERROR)
  private String password;

  public UserModel() {
  }

  public UserModel(String mail, String password) {
    this.mail = mail;
    this.password = password;
  }

}