package com.greenthumb.feature.species;

import com.greenthumb.feature.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a species in the global plant catalogue.
 * <p>
 * Managed by admins. Users link their personal plants to a species
 * via the {@link com.greenthumb.feature.plant.UserPlant} entity.
 * </p>
 *
 * @author Hamza Ali
 */
@Entity
@Table(name = "plant_species")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantSpecies {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Common everyday name (e.g. "Peace Lily"). */
    @Column(nullable = false)
    private String commonName;

    /** Scientific/botanical name (e.g. "Spathiphyllum wallisii"). */
    private String scientificName;

    /** Recommended days between watering sessions. */
    private Integer wateringFrequencyDays;

    /** Light requirement description (e.g. "Bright indirect light"). */
    private String lightRequirement;

    /** Whether this species is toxic to cats — surfaced prominently in UI. */
    @Column(nullable = false)
    @Builder.Default
    private boolean toxicToCats = false;

    /** Cloudinary URL for the species reference image. */
    private String imageUrl;

    /** The admin who added this species to the catalogue. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id")
    private User createdByAdmin;

    /** Timestamp when this species was added. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
