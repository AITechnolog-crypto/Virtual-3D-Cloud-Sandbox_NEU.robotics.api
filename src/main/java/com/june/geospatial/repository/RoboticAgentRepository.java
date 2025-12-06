package com.june.geospatial.repository;

import com.june.geospatial.entity.RoboticAgent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Platzhalter für RoboticAgentRepository
 */
@Repository
public interface RoboticAgentRepository extends MongoRepository<RoboticAgent, String> {
    List<RoboticAgent> findAllByStatus(String status);
}
