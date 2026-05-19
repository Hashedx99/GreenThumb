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
