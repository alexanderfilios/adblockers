package com.adblockers.repos;

import com.adblockers.entities.LegalEntity;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface LegalEntityRepository extends MongoRepository<LegalEntity, String> {

    default void updateAndOverwriteByDomain(Collection<LegalEntity> newLegalEntities) {

        newLegalEntities.forEach(newLegalEntity -> updateAndOverwriteByDomain(newLegalEntity));
    }

    /**
     * Combines all the data of the {@link LegalEntity}s that match the {@link Example} and the new {@link LegalEntity} parameter
     * Then it deletes all old {@link LegalEntity}s and stores one new {@link LegalEntity} that combines the information of all
     * @param newLegalEntity The new {@link LegalEntity}
     */
    default void updateAndOverwriteByDomain(LegalEntity newLegalEntity) {
        // Find matching old legal entities
        LegalEntity exampleLegalEntity = new LegalEntity();
        exampleLegalEntity.setDomain(newLegalEntity.getUrl().getDomain());
        List<LegalEntity> oldLegalEntities = this.findAll(Example.of(exampleLegalEntity));

        // Combine their data with the data of the new legal entity
        LegalEntity combinedLegalEntity = ImmutableList.<LegalEntity>builder()
                .addAll(oldLegalEntities)
                .add(newLegalEntity)
                .build().stream()
                .reduce(new LegalEntity(), (newEntity, oldEntity) -> {
                    if (StringUtils.isEmpty(newEntity.getEntityUrl()) && oldEntity.getEntityUrl() != null) {
                        newEntity.setDomain(oldEntity.getEntityUrl().getDomain());
                    }
                    if (StringUtils.isEmpty(newEntity.getOrganization())) {
                        newEntity.setOrganization(oldEntity.getOrganization());
                    }
                    if (StringUtils.isEmpty(newEntity.getAddress())) {
                        newEntity.setAddress(oldEntity.getAddress());
                    }
                    if (StringUtils.isEmpty(newEntity.getCity())) {
                        newEntity.setCity(oldEntity.getCity());
                    }
                    if (StringUtils.isEmpty(newEntity.getCountry())) {
                        newEntity.setCountry(oldEntity.getCountry());
                    }
                    if (StringUtils.isEmpty(newEntity.getEmail())) {
                        newEntity.setEmail(oldEntity.getEmail());
                    }
                    return newEntity;
                });

        // Overwrite
        this.delete(oldLegalEntities);
        this.save(combinedLegalEntity);
    }
}
