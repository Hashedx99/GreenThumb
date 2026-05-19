package com.greenthumb.feature.review;

import com.greenthumb.feature.plant.UserPlant;
import com.greenthumb.feature.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a user's review of a plant species,
 * written through their personal plant experience.
 *
 * @author Hamza Ali
 */
@Entity
@Table(name = "plant_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantReview {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The specific plant the user is reviewing. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_plant_id", nullable = false)
    private UserPlant userPlant;

    /** The user writing the review. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Star rating from 1 to 5. */
    @Column(nullable = false)
    private Integer rating;

    /** Written review body. */
    @Column(length = 1000)
    private String body;

    /** Timestamp of when the review was written. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
