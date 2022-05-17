package com.spring.exercise.repository;

import com.spring.exercise.model.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;

public interface TicketRepository extends MongoRepository<TicketEntity, String> {
    Page<TicketEntity> findAll(Pageable pageable);
}