package com.greenthumb.feature.species;

import com.greenthumb.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for plant species catalogue endpoints.
 * <p>
 * Public GET endpoints are accessible without authentication.
 * POST, PUT, DELETE endpoints require ADMIN role.
 * </p>
 *
 * @author Hamza Ali
 */
@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class PlantSpeciesController {

    private final PlantSpeciesService speciesService;

    /**
     * Returns a paginated, searchable list of all plant species.
     *
     * @param query    optional search term to filter by name (public)
     * @param pageable pagination parameters
     * @return 200 with page of species
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PlantSpeciesDto.SpeciesResponse>>> getAllSpecies(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        Page<PlantSpeciesDto.SpeciesResponse> species = speciesService.getAllSpecies(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Species retrieved", species));
    }

    /**
     * Returns the details of a single species by ID.
     *
     * @param id the species ID
     * @return 200 with species details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantSpeciesDto.SpeciesResponse>> getSpeciesById(
            @PathVariable Long id) {
        PlantSpeciesDto.SpeciesResponse response = speciesService.getSpeciesById(id);
        return ResponseEntity.ok(ApiResponse.success("Species retrieved", response));
    }

    /**
     * Creates a new species in the catalogue — admin only.
     *
     * @param request     the species creation request body
     * @param userDetails the authenticated admin
     * @return 201 Created with the new species
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlantSpeciesDto.SpeciesResponse>> createSpecies(
            @Valid @RequestBody PlantSpeciesDto.SpeciesRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlantSpeciesDto.SpeciesResponse response =
                speciesService.createSpecies(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Species created successfully", response));
    }

    /**
     * Updates an existing species — admin only.
     *
     * @param id      the species ID to update
     * @param request the update request body
     * @return 200 with updated species
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlantSpeciesDto.SpeciesResponse>> updateSpecies(
            @PathVariable Long id,
            @Valid @RequestBody PlantSpeciesDto.SpeciesRequest request) {
        PlantSpeciesDto.SpeciesResponse response = speciesService.updateSpecies(id, request);
        return ResponseEntity.ok(ApiResponse.success("Species updated successfully", response));
    }

    /**
     * Uploads a reference image for a species — admin only.
     *
     * @param id   the species ID
     * @param file the image file (multipart)
     * @return 200 with updated species including image URL
     */
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlantSpeciesDto.SpeciesResponse>> uploadSpeciesImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        PlantSpeciesDto.SpeciesResponse response = speciesService.uploadSpeciesImage(id, file);
        return ResponseEntity.ok(ApiResponse.success("Species image uploaded", response));
    }

    /**
     * Deletes a species from the catalogue — admin only.
     *
     * @param id the species ID to delete
     * @return 200 success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSpecies(@PathVariable Long id) {
        speciesService.deleteSpecies(id);
        return ResponseEntity.ok(ApiResponse.success("Species deleted successfully"));
    }
}
