package com.adblockers.firstparty;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
public interface FirstPartyRepository extends MongoRepository<FirstParty, String> {
}
