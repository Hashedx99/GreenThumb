package com.greenthumb.feature.species;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link PlantSpecies} persistence and search operations.
 *
 * @author Hamza Ali
 */
@Repository
public interface PlantSpeciesRepository extends JpaRepository<PlantSpecies, Long> {

    /**
     * Searches species by common name or scientific name — case-insensitive.
     *
     * @param query    the search keyword
     * @param pageable pagination parameters
     * @return a page of matching species
     */
    @Query("SELECT s FROM PlantSpecies s WHERE " +
           "LOWER(s.commonName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.scientificName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<PlantSpecies> searchByName(@Param("query") String query, Pageable pageable);

    /**
     * Filters species by cat toxicity flag.
     *
     * @param toxicToCats true to find toxic species, false for safe ones
     * @param pageable    pagination parameters
     * @return a page of species matching the toxicity filter
     */
    Page<PlantSpecies> findByToxicToCats(boolean toxicToCats, Pageable pageable);
}
