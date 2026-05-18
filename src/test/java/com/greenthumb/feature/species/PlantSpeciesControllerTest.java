package com.greenthumb.feature.species;

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
 * Unit tests for {@link PlantSpeciesController} using pure Mockito.
 * Controller methods are called directly — no Spring context loaded.
 *
 * @author Hamza Ali
 */
@ExtendWith(MockitoExtension.class)
class PlantSpeciesControllerTest {

    @Mock
    private PlantSpeciesService speciesService;

    @InjectMocks
    private PlantSpeciesController speciesController;

    private UserDetails adminDetails;
    private PlantSpeciesDto.SpeciesResponse speciesResponse;
    private PlantSpeciesDto.SpeciesRequest speciesRequest;

    /**
     * Builds reusable fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        adminDetails = User.withUsername("admin@test.com")
                .password("hashed").roles("ADMIN").build();

        speciesResponse = PlantSpeciesDto.SpeciesResponse.builder()
                .id(1L).commonName("Peace Lily").scientificName("Spathiphyllum wallisii")
                .wateringFrequencyDays(7).lightRequirement("Low indirect")
                .toxicToCats(true).createdAt(LocalDateTime.now()).build();

        speciesRequest = new PlantSpeciesDto.SpeciesRequest();
        speciesRequest.setCommonName("Peace Lily");
        speciesRequest.setWateringFrequencyDays(7);
    }

    // ── GET /api/species ───────────────────────────────────────────────────

    /**
     * Test: getAllSpecies() returns 200 OK with a page of species.
     */
    @Test
    @DisplayName("getAllSpecies() - no query returns 200 with all species")
    void getAllSpecies_noQuery_returns200WithAllSpecies() {
        Page<PlantSpeciesDto.SpeciesResponse> page = new PageImpl<>(List.of(speciesResponse));
        when(speciesService.getAllSpecies(isNull(), any())).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PlantSpeciesDto.SpeciesResponse>>> response =
                speciesController.getAllSpecies(null, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getContent().get(0).getCommonName())
                .isEqualTo("Peace Lily");
    }

    /**
     * Test: getAllSpecies() passes the query string to the service.
     */
    @Test
    @DisplayName("getAllSpecies() - search query is passed to service")
    void getAllSpecies_withQuery_passesQueryToService() {
        Page<PlantSpeciesDto.SpeciesResponse> page = new PageImpl<>(List.of(speciesResponse));
        when(speciesService.getAllSpecies(eq("peace"), any())).thenReturn(page);

        speciesController.getAllSpecies("peace", PageRequest.of(0, 10));

        verify(speciesService, times(1)).getAllSpecies(eq("peace"), any());
    }

    /**
     * Test: getAllSpecies() returns empty page when no matches found.
     */
    @Test
    @DisplayName("getAllSpecies() - no matches returns empty page")
    void getAllSpecies_noMatches_returnsEmptyPage() {
        when(speciesService.getAllSpecies(any(), any())).thenReturn(Page.empty());

        ResponseEntity<ApiResponse<Page<PlantSpeciesDto.SpeciesResponse>>> response =
                speciesController.getAllSpecies("nonexistent", PageRequest.of(0, 10));

        assertThat(response.getBody().getData().getContent()).isEmpty();
    }

    // ── GET /api/species/{id} ─────────────────────────────────────────────

    /**
     * Test: getSpeciesById() returns 200 OK with species data for a known ID.
     */
    @Test
    @DisplayName("getSpeciesById() - known id returns 200 with species")
    void getSpeciesById_knownId_returns200() {
        when(speciesService.getSpeciesById(1L)).thenReturn(speciesResponse);

        ResponseEntity<ApiResponse<PlantSpeciesDto.SpeciesResponse>> response =
                speciesController.getSpeciesById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getCommonName()).isEqualTo("Peace Lily");
        assertThat(response.getBody().getData().isToxicToCats()).isTrue();
    }

    /**
     * Test: getSpeciesById() propagates ResourceNotFoundException for unknown ID.
     */
    @Test
    @DisplayName("getSpeciesById() - unknown id propagates ResourceNotFoundException")
    void getSpeciesById_unknownId_propagatesResourceNotFoundException() {
        when(speciesService.getSpeciesById(99L))
                .thenThrow(new ResourceNotFoundException("Plant species", 99L));

        assertThatThrownBy(() -> speciesController.getSpeciesById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── POST /api/species ─────────────────────────────────────────────────

    /**
     * Test: createSpecies() returns 201 Created with the new species.
     */
    @Test
    @DisplayName("createSpecies() - valid request returns 201 Created")
    void createSpecies_validRequest_returns201() {
        when(speciesService.createSpecies(any(), eq("admin@test.com")))
                .thenReturn(speciesResponse);

        ResponseEntity<ApiResponse<PlantSpeciesDto.SpeciesResponse>> response =
                speciesController.createSpecies(speciesRequest, adminDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getCommonName()).isEqualTo("Peace Lily");
    }

    /**
     * Test: createSpecies() passes admin email from UserDetails to service.
     */
    @Test
    @DisplayName("createSpecies() - passes admin email to service")
    void createSpecies_passesAdminEmailToService() {
        when(speciesService.createSpecies(any(), anyString())).thenReturn(speciesResponse);

        speciesController.createSpecies(speciesRequest, adminDetails);

        verify(speciesService, times(1)).createSpecies(any(), eq("admin@test.com"));
    }

    // ── PUT /api/species/{id} ─────────────────────────────────────────────

    /**
     * Test: updateSpecies() returns 200 OK with updated species data.
     */
    @Test
    @DisplayName("updateSpecies() - valid request returns 200 OK")
    void updateSpecies_validRequest_returns200() {
        PlantSpeciesDto.SpeciesResponse updated = PlantSpeciesDto.SpeciesResponse.builder()
                .id(1L).commonName("Peace Lily Updated").wateringFrequencyDays(10)
                .toxicToCats(true).createdAt(LocalDateTime.now()).build();

        when(speciesService.updateSpecies(eq(1L), any())).thenReturn(updated);

        ResponseEntity<ApiResponse<PlantSpeciesDto.SpeciesResponse>> response =
                speciesController.updateSpecies(1L, speciesRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getCommonName()).isEqualTo("Peace Lily Updated");
    }

    /**
     * Test: updateSpecies() propagates ResourceNotFoundException for unknown ID.
     */
    @Test
    @DisplayName("updateSpecies() - unknown id propagates ResourceNotFoundException")
    void updateSpecies_unknownId_propagatesResourceNotFoundException() {
        when(speciesService.updateSpecies(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Plant species", 99L));

        assertThatThrownBy(() -> speciesController.updateSpecies(99L, speciesRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── DELETE /api/species/{id} ──────────────────────────────────────────

    /**
     * Test: deleteSpecies() returns 200 OK with success message.
     */
    @Test
    @DisplayName("deleteSpecies() - existing species returns 200 OK")
    void deleteSpecies_existingSpecies_returns200() {
        doNothing().when(speciesService).deleteSpecies(1L);

        ResponseEntity<ApiResponse<Void>> response = speciesController.deleteSpecies(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("deleted");
    }

    /**
     * Test: deleteSpecies() delegates to service with the correct ID.
     */
    @Test
    @DisplayName("deleteSpecies() - delegates to service with correct id")
    void deleteSpecies_delegatesWithCorrectId() {
        doNothing().when(speciesService).deleteSpecies(3L);

        speciesController.deleteSpecies(3L);

        verify(speciesService, times(1)).deleteSpecies(3L);
        verifyNoMoreInteractions(speciesService);
    }

    /**
     * Test: deleteSpecies() propagates ResourceNotFoundException for unknown ID.
     */
    @Test
    @DisplayName("deleteSpecies() - unknown id propagates ResourceNotFoundException")
    void deleteSpecies_unknownId_propagatesResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("Plant species", 99L))
                .when(speciesService).deleteSpecies(99L);

        assertThatThrownBy(() -> speciesController.deleteSpecies(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
