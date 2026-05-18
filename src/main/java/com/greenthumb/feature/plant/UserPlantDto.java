package com.greenthumb.feature.plant;

import com.greenthumb.feature.species.PlantSpeciesDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Objects for user plant collection API requests and responses.
 *
 * @author Hamza Ali
 */
public class UserPlantDto {

    // ── Requests ──────────────────────────────────────────────────────────

    /**
     * Request body for adding a plant to the user's collection.
     */
    @Getter
    @Setter
    public static class AddPlantRequest {

        /** The species ID to link this plant to. */
        @NotNull(message = "Species ID is required")
        private Long speciesId;

        /** A personal nickname for this plant. */
        @NotBlank(message = "Nickname is required")
        private String nickname;

        /** The date the plant was acquired. */
        private LocalDate acquiredDate;

        /** Where in the home the plant is kept. */
        private String location;

        /** Any personal notes about the plant. */
        private String notes;
    }

    /**
     * Request body for updating an existing plant's details.
     */
    @Getter
    @Setter
    public static class UpdatePlantRequest {

        /** Updated nickname. */
        @NotBlank(message = "Nickname is required")
        private String nickname;

        /** Updated home location. */
        private String location;

        /** Updated notes. */
        private String notes;

        /** Updated health status. */
        private PlantStatus status;
    }

    // ── Responses ─────────────────────────────────────────────────────────

    /**
     * Full response payload for a user's plant entry.
     */
    @Getter
    @Builder
    public static class PlantResponse {

        private Long id;
        private String nickname;
        private LocalDate acquiredDate;
        private String location;
        private String photoUrl;
        private String notes;
        private String status;
        private LocalDateTime createdAt;

        /** Embedded species summary — avoids a separate species lookup. */
        private PlantSpeciesDto.SpeciesResponse species;

        /**
         * Maps a {@link UserPlant} entity to a {@link PlantResponse}.
         *
         * @param plant the entity to map
         * @return the corresponding PlantResponse
         */
        public static PlantResponse from(UserPlant plant) {
            return PlantResponse.builder()
                    .id(plant.getId())
                    .nickname(plant.getNickname())
                    .acquiredDate(plant.getAcquiredDate())
                    .location(plant.getLocation())
                    .photoUrl(plant.getPhotoUrl())
                    .notes(plant.getNotes())
                    .status(plant.getStatus().name())
                    .createdAt(plant.getCreatedAt())
                    .species(PlantSpeciesDto.SpeciesResponse.from(plant.getSpecies()))
                    .build();
        }
    }
}
