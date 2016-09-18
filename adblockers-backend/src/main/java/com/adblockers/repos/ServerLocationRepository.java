package com.adblockers.repos;

import com.adblockers.entities.ServerLocation;
import com.sun.org.apache.xpath.internal.operations.String;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface ServerLocationRepository extends MongoRepository<ServerLocation, String> {
}
