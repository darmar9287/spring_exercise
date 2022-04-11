package com.spring.exercise.repository;

import com.spring.exercise.model.UserModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserModel, ObjectId> {

    UserModel findByEmail(String email);
}
