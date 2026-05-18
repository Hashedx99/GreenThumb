package com.greenthumb.feature.plant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for user plant collection operations.
 *
 * @author Hamza Ali
 */
public interface UserPlantService {

    /**
     * Returns a paginated list of all plants in the authenticated user's collection.
     *
     * @param userEmail the authenticated user's email
     * @param pageable  pagination parameters
     * @return a page of plant responses
     */
    Page<UserPlantDto.PlantResponse> getMyPlants(String userEmail, Pageable pageable);

    /**
     * Returns the details of a single plant by ID — validates ownership.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID
     * @return the plant response DTO
     */
    UserPlantDto.PlantResponse getPlantById(String userEmail, Long plantId);

    /**
     * Adds a new plant to the user's collection.
     *
     * @param userEmail the authenticated user's email
     * @param request   the add plant request body
     * @return the created plant response DTO
     */
    UserPlantDto.PlantResponse addPlant(String userEmail, UserPlantDto.AddPlantRequest request);

    /**
     * Updates an existing plant's details — validates ownership.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID to update
     * @param request   the update request body
     * @return the updated plant response DTO
     */
    UserPlantDto.PlantResponse updatePlant(String userEmail, Long plantId,
                                           UserPlantDto.UpdatePlantRequest request);

    /**
     * Uploads a photo for a plant — validates ownership.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID
     * @param file      the image file to upload
     * @return the updated plant response DTO with new photo URL
     */
    UserPlantDto.PlantResponse uploadPlantPhoto(String userEmail, Long plantId, MultipartFile file);

    /**
     * Removes a plant from the user's collection — validates ownership.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID to remove
     */
    void removePlant(String userEmail, Long plantId);

    /**
     * Returns all plants in the user's collection that have care due today or overdue.
     *
     * @param userEmail the authenticated user's email
     * @return list of plants needing care
     */
    List<UserPlantDto.PlantResponse> getPlantsDueToday(String userEmail);
}
