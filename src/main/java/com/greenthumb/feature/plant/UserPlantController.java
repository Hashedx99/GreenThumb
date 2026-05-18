package com.greenthumb.feature.plant;

import com.greenthumb.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for user plant collection endpoints.
 * All routes require authentication (JWT).
 *
 * @author Hamza Ali
 */
@RestController
@RequestMapping("/api/my-plants")
@RequiredArgsConstructor
public class UserPlantController {

    private final UserPlantService plantService;

    /**
     * Returns the authenticated user's plant collection with pagination.
     *
     * @param userDetails the authenticated user
     * @param pageable    pagination parameters
     * @return 200 with page of plants
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserPlantDto.PlantResponse>>> getMyPlants(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<UserPlantDto.PlantResponse> plants =
                plantService.getMyPlants(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Plants retrieved", plants));
    }

    /**
     * Returns all plants that have care due today or are overdue.
     *
     * @param userDetails the authenticated user
     * @return 200 with list of plants needing care
     */
    @GetMapping("/due-today")
    public ResponseEntity<ApiResponse<List<UserPlantDto.PlantResponse>>> getPlantsDueToday(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<UserPlantDto.PlantResponse> plants =
                plantService.getPlantsDueToday(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Plants due for care retrieved", plants));
    }

    /**
     * Returns details of a single plant by ID — validates ownership.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID
     * @return 200 with plant details
     */
    @GetMapping("/{plantId}")
    public ResponseEntity<ApiResponse<UserPlantDto.PlantResponse>> getPlantById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId) {
        UserPlantDto.PlantResponse plant =
                plantService.getPlantById(userDetails.getUsername(), plantId);
        return ResponseEntity.ok(ApiResponse.success("Plant retrieved", plant));
    }

    /**
     * Adds a new plant to the authenticated user's collection.
     *
     * @param userDetails the authenticated user
     * @param request     the add plant request body
     * @return 201 Created with the new plant
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserPlantDto.PlantResponse>> addPlant(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserPlantDto.AddPlantRequest request) {
        UserPlantDto.PlantResponse plant =
                plantService.addPlant(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plant added to your collection", plant));
    }

    /**
     * Updates an existing plant's details — validates ownership.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID to update
     * @param request     the update request body
     * @return 200 with updated plant
     */
    @PutMapping("/{plantId}")
    public ResponseEntity<ApiResponse<UserPlantDto.PlantResponse>> updatePlant(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @Valid @RequestBody UserPlantDto.UpdatePlantRequest request) {
        UserPlantDto.PlantResponse plant =
                plantService.updatePlant(userDetails.getUsername(), plantId, request);
        return ResponseEntity.ok(ApiResponse.success("Plant updated successfully", plant));
    }

    /**
     * Uploads a photo for a specific plant — validates ownership.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID
     * @param file        the image file (multipart)
     * @return 200 with updated plant including photo URL
     */
    @PostMapping("/{plantId}/photo")
    public ResponseEntity<ApiResponse<UserPlantDto.PlantResponse>> uploadPlantPhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @RequestParam("file") MultipartFile file) {
        UserPlantDto.PlantResponse plant =
                plantService.uploadPlantPhoto(userDetails.getUsername(), plantId, file);
        return ResponseEntity.ok(ApiResponse.success("Plant photo uploaded", plant));
    }

    /**
     * Removes a plant from the user's collection — validates ownership.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID to remove
     * @return 200 success message
     */
    @DeleteMapping("/{plantId}")
    public ResponseEntity<ApiResponse<Void>> removePlant(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId) {
        plantService.removePlant(userDetails.getUsername(), plantId);
        return ResponseEntity.ok(ApiResponse.success("Plant removed from your collection"));
    }
}
