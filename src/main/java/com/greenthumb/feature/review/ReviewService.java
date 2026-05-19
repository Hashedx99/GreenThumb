package com.greenthumb.feature.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for plant review operations.
 *
 * @author Hamza Ali
 */
public interface ReviewService {

    /**
     * Creates a review for a species via a user's plant.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the user's plant ID (must own the plant)
     * @param request   the review creation request
     * @return the created review response
     */
    ReviewDto.ReviewResponse createReview(String userEmail, Long plantId,
                                          ReviewDto.ReviewRequest request);

    /**
     * Updates an existing review — only the original reviewer can update.
     *
     * @param userEmail the authenticated user's email
     * @param reviewId  the review ID to update
     * @param request   the update request
     * @return the updated review response
     */
    ReviewDto.ReviewResponse updateReview(String userEmail, Long reviewId,
                                          ReviewDto.ReviewRequest request);

    /**
     * Deletes a review — only the original reviewer can delete.
     *
     * @param userEmail the authenticated user's email
     * @param reviewId  the review ID to delete
     */
    void deleteReview(String userEmail, Long reviewId);

    /**
     * Returns paginated reviews for a given species.
     *
     * @param speciesId the species ID
     * @param pageable  pagination parameters
     * @return page of reviews for that species
     */
    Page<ReviewDto.ReviewResponse> getReviewsBySpecies(Long speciesId, Pageable pageable);
}
