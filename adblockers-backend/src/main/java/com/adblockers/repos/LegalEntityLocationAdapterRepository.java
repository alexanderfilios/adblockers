package com.adblockers.repos;

import com.adblockers.entities.LegalEntityLocationAdapter;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by alexandrosfilios on 21/10/16.
 */
public interface LegalEntityLocationAdapterRepository extends MongoRepository<LegalEntityLocationAdapter, String> {
}
