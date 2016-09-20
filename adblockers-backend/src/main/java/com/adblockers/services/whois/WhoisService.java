package com.adblockers.services.whois;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.Url;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface WhoisService {
    LegalEntity findLegalEntityByUrl(@NotNull Url url);

    /**
     * Allows for a possibly more effective way of getting multiple urls altogether
     * As a fallback solution, URLs are going to be searched une by one
     * @param urls
     * @return
     */
    default Set<LegalEntity> findLegalEntitiesByUrl(@NotNull Set<Url> urls) {
        return urls.stream()
                .map(url -> findLegalEntityByUrl(url))
                .filter(legalEntity -> legalEntity != null)
                .collect(Collectors.toSet());
    }
}
