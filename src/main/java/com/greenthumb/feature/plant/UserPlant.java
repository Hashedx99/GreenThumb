package com.greenthumb.feature.plant;

import com.greenthumb.feature.species.PlantSpecies;
import com.greenthumb.feature.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a plant in a user's personal collection.
 * <p>
 * Links a {@link User} to a {@link PlantSpecies} with personal
 * details like nickname, location within the home, and health status.
 * </p>
 *
 * @author Hamza Ali
 */
@Entity
@Table(name = "user_plants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlant {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who owns this plant. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The species this plant belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private PlantSpecies species;

    /** User-given nickname for this specific plant (e.g. "Kevin"). */
    @Column(nullable = false)
    private String nickname;

    /** The date the user acquired this plant. */
    private LocalDate acquiredDate;

    /** Where in the home the plant lives (e.g. "Living Room Windowsill"). */
    private String location;

    /** Cloudinary URL for the user's own photo of this plant. */
    private String photoUrl;

    /** Personal notes about care history or observations. */
    @Column(length = 1000)
    private String notes;

    /** Current health status of the plant. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlantStatus status = PlantStatus.HEALTHY;

    /** Timestamp when this plant was added to the collection. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
