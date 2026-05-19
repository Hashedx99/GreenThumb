package com.greenthumb.feature.review;

import com.greenthumb.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for plant review endpoints.
 *
 * @author Hamza Ali
 */
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Creates a review for a species via the user's plant.
     *
     * @param userDetails the authenticated user
     * @param plantId     the user's plant ID
     * @param request     the review request body
     * @return 201 Created with the new review
     */
    @PostMapping("/api/my-plants/{plantId}/reviews")
    public ResponseEntity<ApiResponse<ReviewDto.ReviewResponse>> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @Valid @RequestBody ReviewDto.ReviewRequest request) {
        ReviewDto.ReviewResponse review =
                reviewService.createReview(userDetails.getUsername(), plantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted", review));
    }

    /**
     * Updates an existing review — restricted to the original reviewer.
     *
     * @param userDetails the authenticated user
     * @param reviewId    the review ID to update
     * @param request     the update request body
     * @return 200 with updated review
     */
    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto.ReviewResponse>> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewDto.ReviewRequest request) {
        ReviewDto.ReviewResponse review =
                reviewService.updateReview(userDetails.getUsername(), reviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Review updated", review));
    }

    /**
     * Deletes a review — restricted to the original reviewer.
     *
     * @param userDetails the authenticated user
     * @param reviewId    the review ID to delete
     * @return 200 success message
     */
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(userDetails.getUsername(), reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }

    /**
     * Returns paginated reviews for a given species — public endpoint.
     *
     * @param speciesId the species ID
     * @param pageable  pagination parameters
     * @return 200 with page of reviews
     */
    @GetMapping("/api/species/{speciesId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDto.ReviewResponse>>> getReviewsBySpecies(
            @PathVariable Long speciesId,
            Pageable pageable) {
        Page<ReviewDto.ReviewResponse> reviews =
                reviewService.getReviewsBySpecies(speciesId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", reviews));
    }
}
