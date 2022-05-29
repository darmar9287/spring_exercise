package com.spring.exercise.repository;

import com.spring.exercise.model.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<OrderEntity, String> {
    List<OrderEntity> findAllByUserId(String userId);
}