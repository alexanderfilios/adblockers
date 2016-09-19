package com.adblockers.repos;

import com.adblockers.entities.BrowserProfile;
import com.adblockers.entities.Metric;
import com.adblockers.services.requestgraph.RequestGraph;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface MetricRepository extends MongoRepository<Metric, String> {
}
