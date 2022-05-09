package com.spring.exercise.repository;

import com.spring.exercise.model.TicketEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketRepository extends MongoRepository<TicketEntity, String> {
}
