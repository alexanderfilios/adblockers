package com.adblockers.repos;

import com.adblockers.entities.MetricAdapter;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by alexandrosfilios on 20/10/16.
 */
public interface MetricAdapterRepository extends MongoRepository<MetricAdapter, String> {
}
