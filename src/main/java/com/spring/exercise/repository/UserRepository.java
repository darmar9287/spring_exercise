package com.spring.exercise.repository;

import com.spring.exercise.model.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {

    Optional<UserEntity> findByUserName(String username);
}
