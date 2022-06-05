package com.spring.exercise.repository;

import com.spring.exercise.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketRepository extends MongoRepository<TicketEntity, String> {
    Page<TicketEntity> findAll(Pageable pageable);
}