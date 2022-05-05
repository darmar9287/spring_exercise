package com.spring.exercise.repository;

import com.spring.exercise.model.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByUserName(String username);
    void delete(UserEntity user);

}
