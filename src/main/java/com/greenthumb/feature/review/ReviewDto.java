package com.greenthumb.feature.review;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Objects for plant review API interactions.
 *
 * @author Hamza Ali
 */
public class ReviewDto {

    /**
     * Request body for creating or updating a plant review.
     */
    @Getter
    @Setter
    public static class ReviewRequest {

        /** Star rating — must be between 1 and 5. */
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        private Integer rating;

        /** Written review body. */
        @Size(max = 1000, message = "Review body must not exceed 1000 characters")
        private String body;
    }

    /**
     * Response payload for a plant review.
     */
    @Getter
    @Builder
    public static class ReviewResponse {

        private Long id;
        private Integer rating;
        private String body;
        private LocalDateTime createdAt;
        private String reviewerName;
        private Long speciesId;
        private String speciesCommonName;

        /**
         * Maps a {@link PlantReview} entity to a {@link ReviewResponse}.
         *
         * @param review the entity to map
         * @return the corresponding ReviewResponse
         */
        public static ReviewResponse from(PlantReview review) {
            return ReviewResponse.builder()
                    .id(review.getId())
                    .rating(review.getRating())
                    .body(review.getBody())
                    .createdAt(review.getCreatedAt())
                    .reviewerName(review.getUser().getName())
                    .speciesId(review.getUserPlant().getSpecies().getId())
                    .speciesCommonName(review.getUserPlant().getSpecies().getCommonName())
                    .build();
        }
    }
}
