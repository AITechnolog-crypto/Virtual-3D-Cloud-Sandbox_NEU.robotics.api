package com.june.geospatial.repository;

import com.june.geospatial.entity.PlaceCache;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Platzhalter für PlaceCacheRepository
 */
@Repository
public interface PlaceCacheRepository extends MongoRepository<PlaceCache, String> {
}
