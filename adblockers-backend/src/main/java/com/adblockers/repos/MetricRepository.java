package com.adblockers.repos;

import com.adblockers.entities.Metric;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface MetricRepository extends MongoRepository<Metric, String> {
}
