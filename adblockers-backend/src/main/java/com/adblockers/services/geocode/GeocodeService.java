package com.adblockers.services.geocode;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.LegalEntityLocation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public interface GeocodeService {
    LegalEntityLocation findLocationByLegalEntity(LegalEntity legalEntity);

    default List<LegalEntityLocation> findLocationsByLegalEntity(List<LegalEntity> legalEntities) {
        return legalEntities.stream()
                .map(legalEntity -> findLocationByLegalEntity(legalEntity))
                .collect(Collectors.toList());
    }
}
