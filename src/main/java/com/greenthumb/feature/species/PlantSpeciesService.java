package com.greenthumb.feature.species;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for plant species catalogue operations.
 *
 * @author Hamza Ali
 */
public interface PlantSpeciesService {

    /**
     * Returns a paginated list of all species, optionally filtered by search query.
     *
     * @param query    optional search term to filter by name
     * @param pageable pagination parameters
     * @return a page of species responses
     */
    Page<PlantSpeciesDto.SpeciesResponse> getAllSpecies(String query, Pageable pageable);

    /**
     * Returns the details of a single species by ID.
     *
     * @param id the species ID
     * @return the species response DTO
     */
    PlantSpeciesDto.SpeciesResponse getSpeciesById(Long id);

    /**
     * Creates a new species in the catalogue — admin only.
     *
     * @param request    the species creation request
     * @param adminEmail the email of the admin creating the species
     * @return the created species response DTO
     */
    PlantSpeciesDto.SpeciesResponse createSpecies(PlantSpeciesDto.SpeciesRequest request, String adminEmail);

    /**
     * Updates an existing species — admin only.
     *
     * @param id      the species ID to update
     * @param request the update request body
     * @return the updated species response DTO
     */
    PlantSpeciesDto.SpeciesResponse updateSpecies(Long id, PlantSpeciesDto.SpeciesRequest request);

    /**
     * Uploads a reference image for a species — admin only.
     *
     * @param id   the species ID
     * @param file the image file to upload
     * @return the updated species response DTO with new image URL
     */
    PlantSpeciesDto.SpeciesResponse uploadSpeciesImage(Long id, MultipartFile file);

    /**
     * Deletes a species from the catalogue — admin only.
     *
     * @param id the species ID to delete
     */
    void deleteSpecies(Long id);
}
