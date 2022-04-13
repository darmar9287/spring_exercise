package com.spring.exercise.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Getter @Setter
public class UserModel {

  @Id
  private ObjectId id;

  @Indexed(unique = true)
  private String email;
  private String password;

  public UserModel() {
  }

  public UserModel(String email, String password) {
    this.email = email;
    this.password = password;
  }

}