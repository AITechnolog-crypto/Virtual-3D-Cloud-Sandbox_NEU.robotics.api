package com.june.geospatial.repository;

import com.june.geospatial.entity.MissionFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Platzhalter für MissionFeedbackRepository
 */
@Repository
public interface MissionFeedbackRepository extends MongoRepository<MissionFeedback, String> {
}
