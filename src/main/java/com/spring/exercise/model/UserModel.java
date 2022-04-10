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

  @CreatedDate
  private LocalDateTime createdAt;
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  @Indexed(unique = true)
  private String username;
  private String password;
  @Indexed
  private String email;

  public UserModel() {
  }

  public UserModel(String username, String password, String email) {
    this.username = username;
    this.password = password;
    this.email = email;
  }

}