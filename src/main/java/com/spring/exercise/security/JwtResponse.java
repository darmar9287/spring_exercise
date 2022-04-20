package com.spring.exercise.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class JwtResponse implements Serializable {
    private String token;
    private String type = "Bearer";
    private ObjectId id;
    private String userName;

    public JwtResponse(String token, ObjectId id, String userName) {
        this.token = token;
        this.id = id;
        this.userName = userName;
    }

}
