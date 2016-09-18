package com.adblockers.repos;

import com.adblockers.entities.LegalEntityLocation;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface LegalEntityLocationRepository extends MongoRepository<LegalEntityLocation, String> {
}
