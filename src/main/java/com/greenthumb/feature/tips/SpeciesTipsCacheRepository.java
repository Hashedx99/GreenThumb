package com.greenthumb.feature.tips;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link SpeciesTipsCache} persistence.
 *
 * @author Hamza Ali
 */
@Repository
public interface SpeciesTipsCacheRepository extends JpaRepository<SpeciesTipsCache, Long> {

    /**
     * Finds a cached tips entry for a given species.
     *
     * @param speciesId the species ID
     * @return Optional cache entry
     */
    Optional<SpeciesTipsCache> findBySpeciesId(Long speciesId);
}
