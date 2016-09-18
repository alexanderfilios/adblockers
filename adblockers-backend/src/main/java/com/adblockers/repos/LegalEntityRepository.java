package com.adblockers.repos;

import com.adblockers.entities.LegalEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface LegalEntityRepository extends MongoRepository<LegalEntity, String> {
}
