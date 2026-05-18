package com.greenthumb.feature.species;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Objects for plant species API requests and responses.
 *
 * @author Hamza Ali
 */
public class PlantSpeciesDto {

    // ── Requests ──────────────────────────────────────────────────────────

    /**
     * Request body for creating or updating a plant species (admin only).
     */
    @Getter
    @Setter
    public static class SpeciesRequest {

        /** Common name is required for all species entries. */
        @NotBlank(message = "Common name is required")
        private String commonName;

        /** Optional scientific/botanical name. */
        private String scientificName;

        /** Recommended watering interval in days — must be positive. */
        @Positive(message = "Watering frequency must be a positive number of days")
        private Integer wateringFrequencyDays;

        /** Description of light needs (e.g. "Indirect bright light"). */
        private String lightRequirement;

        /** Whether the species is toxic to cats. Defaults to false. */
        private boolean toxicToCats;
    }

    // ── Responses ─────────────────────────────────────────────────────────

    /**
     * Full response payload for a plant species.
     */
    @Getter
    @Builder
    public static class SpeciesResponse {

        private Long id;
        private String commonName;
        private String scientificName;
        private Integer wateringFrequencyDays;
        private String lightRequirement;
        private boolean toxicToCats;
        private String imageUrl;
        private LocalDateTime createdAt;

        /**
         * Maps a {@link PlantSpecies} entity to a {@link SpeciesResponse}.
         *
         * @param species the entity to map
         * @return the corresponding SpeciesResponse
         */
        public static SpeciesResponse from(PlantSpecies species) {
            return SpeciesResponse.builder()
                    .id(species.getId())
                    .commonName(species.getCommonName())
                    .scientificName(species.getScientificName())
                    .wateringFrequencyDays(species.getWateringFrequencyDays())
                    .lightRequirement(species.getLightRequirement())
                    .toxicToCats(species.isToxicToCats())
                    .imageUrl(species.getImageUrl())
                    .createdAt(species.getCreatedAt())
                    .build();
        }
    }
}
