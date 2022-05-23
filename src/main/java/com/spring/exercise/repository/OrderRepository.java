package com.spring.exercise.repository;

import com.spring.exercise.model.OrderEntity;
import com.spring.exercise.model.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<OrderEntity, String> {
    @Query("{userId :?0}")
    List<OrderEntity> getTicketOrdersForUser(String userId);
}