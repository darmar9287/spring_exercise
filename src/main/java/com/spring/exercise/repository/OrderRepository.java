package com.spring.exercise.repository;

import com.spring.exercise.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<OrderEntity, String> {
    Page<OrderEntity> findAllByUserId(String userId, Pageable pageable);
}