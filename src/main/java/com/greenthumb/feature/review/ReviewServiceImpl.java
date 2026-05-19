package com.greenthumb.feature.review;

import com.greenthumb.feature.plant.UserPlant;
import com.greenthumb.feature.plant.UserPlantRepository;
import com.greenthumb.feature.user.User;
import com.greenthumb.feature.user.UserRepository;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ReviewService} for plant review management.
 *
 * @author Hamza Ali
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final PlantReviewRepository reviewRepository;
    private final UserPlantRepository plantRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     * Enforces one review per user per plant.
     */
    @Override
    @Transactional
    public ReviewDto.ReviewResponse createReview(String userEmail, Long plantId,
                                                  ReviewDto.ReviewRequest request) {
        User user = findUserByEmail(userEmail);
        UserPlant plant = plantRepository.findByIdAndUserId(plantId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Plant not found: " + plantId));

        // Enforce one review per plant per user
        reviewRepository.findByUserPlantIdAndUserId(plantId, user.getId())
                .ifPresent(r -> {
                    throw new BusinessException("You have already reviewed this plant.");
                });

        PlantReview review = PlantReview.builder()
                .userPlant(plant)
                .user(user)
                .rating(request.getRating())
                .body(request.getBody())
                .build();

        PlantReview saved = reviewRepository.save(review);
        log.info("Review created: id={} by user: {}", saved.getId(), userEmail);
        return ReviewDto.ReviewResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ReviewDto.ReviewResponse updateReview(String userEmail, Long reviewId,
                                                  ReviewDto.ReviewRequest request) {
        User user = findUserByEmail(userEmail);
        PlantReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        // Only the original reviewer can update their review
        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You can only edit your own reviews.");
        }

        review.setRating(request.getRating());
        review.setBody(request.getBody());

        PlantReview saved = reviewRepository.save(review);
        log.info("Review updated: id={}", reviewId);
        return ReviewDto.ReviewResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteReview(String userEmail, Long reviewId) {
        User user = findUserByEmail(userEmail);
        PlantReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        // Only the original reviewer can delete their review
        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You can only delete your own reviews.");
        }

        reviewRepository.delete(review);
        log.info("Review deleted: id={}", reviewId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ReviewDto.ReviewResponse> getReviewsBySpecies(Long speciesId, Pageable pageable) {
        return reviewRepository.findBySpeciesId(speciesId, pageable)
                .map(ReviewDto.ReviewResponse::from);
    }

    /**
     * Looks up a user by email or throws 404.
     *
     * @param email the user's email
     * @return the User entity
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
