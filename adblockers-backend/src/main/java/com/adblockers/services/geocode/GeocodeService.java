package com.adblockers.services.geocode;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.LegalEntityLocation;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public interface GeocodeService {
    Collection<LegalEntity> findLegalEntitiesWithoutGeocodeInformation();

    LegalEntityLocation findLocationByLegalEntity(@NotNull LegalEntity legalEntity);

    default Collection<LegalEntityLocation> findLocationsByLegalEntity(@NotNull Collection<LegalEntity> legalEntities) {
        return legalEntities.stream()
                .filter(legalEntity -> legalEntity.getUrl() != null)
                .map(legalEntity -> findLocationByLegalEntity(legalEntity))
                .filter(legalEntityLocation -> legalEntityLocation != null)
                .collect(Collectors.toList());
    }
}
