package com.greenthumb.feature.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link PlantReview} persistence operations.
 *
 * @author Hamza Ali
 */
@Repository
public interface PlantReviewRepository extends JpaRepository<PlantReview, Long> {

    /**
     * Returns all reviews for a given species (via plant → species link).
     *
     * @param speciesId the species ID
     * @param pageable  pagination parameters
     * @return page of reviews for that species
     */
    @Query("SELECT r FROM PlantReview r WHERE r.userPlant.species.id = :speciesId")
    Page<PlantReview> findBySpeciesId(@Param("speciesId") Long speciesId, Pageable pageable);

    /**
     * Returns all reviews written by a specific user.
     *
     * @param userId   the user's ID
     * @param pageable pagination parameters
     * @return page of reviews by that user
     */
    Page<PlantReview> findByUserId(Long userId, Pageable pageable);

    /**
     * Finds a review by user plant and user — ensures one review per plant per user.
     *
     * @param userPlantId the plant ID
     * @param userId      the user's ID
     * @return Optional review if it exists
     */
    Optional<PlantReview> findByUserPlantIdAndUserId(Long userPlantId, Long userId);

    /**
     * Calculates the average rating for a given species.
     *
     * @param speciesId the species ID
     * @return the average rating, or null if no reviews exist
     */
    @Query("SELECT AVG(r.rating) FROM PlantReview r WHERE r.userPlant.species.id = :speciesId")
    Double findAverageRatingBySpeciesId(@Param("speciesId") Long speciesId);
}
