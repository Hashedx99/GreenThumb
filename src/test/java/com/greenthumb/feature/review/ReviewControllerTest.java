package com.greenthumb.feature.review;

import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import com.greenthumb.shared.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReviewController} using pure Mockito.
 * Controller methods are called directly — no Spring context loaded.
 *
 * @author Hamza Ali
 */
@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private UserDetails userDetails;
    private ReviewDto.ReviewResponse reviewResponse;
    private ReviewDto.ReviewRequest reviewRequest;

    /**
     * Builds reusable fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("hamza@test.com")
                .password("hashed").roles("USER").build();

        reviewResponse = ReviewDto.ReviewResponse.builder()
                .id(1L).rating(5).body("Beautiful plant, highly recommend!")
                .reviewerName("Hamza Ali").speciesId(1L)
                .speciesCommonName("Peace Lily").createdAt(LocalDateTime.now()).build();

        reviewRequest = new ReviewDto.ReviewRequest();
        reviewRequest.setRating(5);
        reviewRequest.setBody("Absolutely love this plant!");
    }

    // ── POST /api/my-plants/{id}/reviews ─────────────────────────────────

    /**
     * Test: createReview() returns 201 Created with the new review.
     */
    @Test
    @DisplayName("createReview() - valid request returns 201 Created")
    void createReview_validRequest_returns201() {
        when(reviewService.createReview(anyString(), anyLong(), any()))
                .thenReturn(reviewResponse);

        ResponseEntity<ApiResponse<ReviewDto.ReviewResponse>> response =
                reviewController.createReview(userDetails, 1L, reviewRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getRating()).isEqualTo(5);
        assertThat(response.getBody().getData().getReviewerName()).isEqualTo("Hamza Ali");
    }

    /**
     * Test: createReview() delegates to service with email from UserDetails.
     */
    @Test
    @DisplayName("createReview() - delegates with email from UserDetails")
    void createReview_delegatesWithUserEmail() {
        when(reviewService.createReview(anyString(), anyLong(), any()))
                .thenReturn(reviewResponse);

        reviewController.createReview(userDetails, 1L, reviewRequest);

        verify(reviewService, times(1)).createReview("hamza@test.com", 1L, reviewRequest);
    }

    /**
     * Test: createReview() propagates BusinessException when user already reviewed.
     */
    @Test
    @DisplayName("createReview() - duplicate review propagates BusinessException")
    void createReview_duplicateReview_propagatesBusinessException() {
        when(reviewService.createReview(anyString(), anyLong(), any()))
                .thenThrow(new BusinessException("You have already reviewed this plant."));

        assertThatThrownBy(() -> reviewController.createReview(userDetails, 1L, reviewRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already reviewed");
    }

    /**
     * Test: createReview() propagates ResourceNotFoundException when plant not owned.
     */
    @Test
    @DisplayName("createReview() - unowned plant propagates ResourceNotFoundException")
    void createReview_unownedPlant_propagatesResourceNotFoundException() {
        when(reviewService.createReview(anyString(), eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Plant not found: 99"));

        assertThatThrownBy(() -> reviewController.createReview(userDetails, 99L, reviewRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── PUT /api/reviews/{id} ─────────────────────────────────────────────

    /**
     * Test: updateReview() returns 200 OK with updated review.
     */
    @Test
    @DisplayName("updateReview() - valid request returns 200 OK")
    void updateReview_validRequest_returns200() {
        ReviewDto.ReviewResponse updated = ReviewDto.ReviewResponse.builder()
                .id(1L).rating(4).body("Still great, minor update")
                .reviewerName("Hamza Ali").speciesId(1L)
                .speciesCommonName("Peace Lily").createdAt(LocalDateTime.now()).build();

        ReviewDto.ReviewRequest updateRequest = new ReviewDto.ReviewRequest();
        updateRequest.setRating(4);
        updateRequest.setBody("Still great, minor update");

        when(reviewService.updateReview(anyString(), anyLong(), any())).thenReturn(updated);

        ResponseEntity<ApiResponse<ReviewDto.ReviewResponse>> response =
                reviewController.updateReview(userDetails, 1L, updateRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getRating()).isEqualTo(4);
    }

    /**
     * Test: updateReview() propagates BusinessException when user is not the author.
     */
    @Test
    @DisplayName("updateReview() - non-author propagates BusinessException")
    void updateReview_nonAuthor_propagatesBusinessException() {
        when(reviewService.updateReview(anyString(), anyLong(), any()))
                .thenThrow(new BusinessException("You can only edit your own reviews."));

        assertThatThrownBy(() -> reviewController.updateReview(userDetails, 1L, reviewRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("own reviews");
    }

    /**
     * Test: updateReview() propagates ResourceNotFoundException for unknown review ID.
     */
    @Test
    @DisplayName("updateReview() - unknown review id propagates ResourceNotFoundException")
    void updateReview_unknownId_propagatesResourceNotFoundException() {
        when(reviewService.updateReview(anyString(), eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Review", 99L));

        assertThatThrownBy(() -> reviewController.updateReview(userDetails, 99L, reviewRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── DELETE /api/reviews/{id} ──────────────────────────────────────────

    /**
     * Test: deleteReview() returns 200 OK for the original reviewer.
     */
    @Test
    @DisplayName("deleteReview() - original reviewer returns 200 OK")
    void deleteReview_originalReviewer_returns200() {
        doNothing().when(reviewService).deleteReview("hamza@test.com", 1L);

        ResponseEntity<ApiResponse<Void>> response =
                reviewController.deleteReview(userDetails, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("deleted");
    }

    /**
     * Test: deleteReview() delegates to service with correct email and review ID.
     */
    @Test
    @DisplayName("deleteReview() - delegates with correct email and review id")
    void deleteReview_delegatesWithCorrectArgs() {
        doNothing().when(reviewService).deleteReview(anyString(), anyLong());

        reviewController.deleteReview(userDetails, 1L);

        verify(reviewService, times(1)).deleteReview("hamza@test.com", 1L);
    }

    /**
     * Test: deleteReview() propagates BusinessException when user is not the author.
     */
    @Test
    @DisplayName("deleteReview() - non-author propagates BusinessException")
    void deleteReview_nonAuthor_propagatesBusinessException() {
        doThrow(new BusinessException("You can only delete your own reviews."))
                .when(reviewService).deleteReview(anyString(), anyLong());

        assertThatThrownBy(() -> reviewController.deleteReview(userDetails, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("own reviews");
    }

    // ── GET /api/species/{id}/reviews ─────────────────────────────────────

    /**
     * Test: getReviewsBySpecies() returns 200 OK with a page of reviews.
     */
    @Test
    @DisplayName("getReviewsBySpecies() - known species returns 200 with reviews")
    void getReviewsBySpecies_knownSpecies_returns200WithReviews() {
        Page<ReviewDto.ReviewResponse> page = new PageImpl<>(List.of(reviewResponse));
        when(reviewService.getReviewsBySpecies(eq(1L), any())).thenReturn(page);

        ResponseEntity<ApiResponse<Page<ReviewDto.ReviewResponse>>> response =
                reviewController.getReviewsBySpecies(1L, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getContent().get(0).getRating()).isEqualTo(5);
    }

    /**
     * Test: getReviewsBySpecies() returns 200 with empty page for species with no reviews.
     */
    @Test
    @DisplayName("getReviewsBySpecies() - no reviews returns empty page")
    void getReviewsBySpecies_noReviews_returnsEmptyPage() {
        when(reviewService.getReviewsBySpecies(anyLong(), any())).thenReturn(Page.empty());

        ResponseEntity<ApiResponse<Page<ReviewDto.ReviewResponse>>> response =
                reviewController.getReviewsBySpecies(1L, PageRequest.of(0, 10));

        assertThat(response.getBody().getData().getContent()).isEmpty();
    }

    /**
     * Test: getReviewsBySpecies() delegates to service with correct species ID.
     */
    @Test
    @DisplayName("getReviewsBySpecies() - delegates with correct species id")
    void getReviewsBySpecies_delegatesWithCorrectSpeciesId() {
        when(reviewService.getReviewsBySpecies(anyLong(), any())).thenReturn(Page.empty());

        reviewController.getReviewsBySpecies(3L, PageRequest.of(0, 10));

        verify(reviewService, times(1)).getReviewsBySpecies(eq(3L), any());
    }
}
