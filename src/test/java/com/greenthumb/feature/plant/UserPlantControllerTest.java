package com.greenthumb.feature.plant;

import com.greenthumb.feature.species.PlantSpeciesDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserPlantController} using pure Mockito.
 * Controller methods are called directly — no Spring context loaded.
 *
 * @author Hamza Ali
 */
@ExtendWith(MockitoExtension.class)
class UserPlantControllerTest {

    @Mock
    private UserPlantService plantService;

    @InjectMocks
    private UserPlantController plantController;

    private UserDetails userDetails;
    private UserPlantDto.PlantResponse plantResponse;

    /**
     * Builds reusable fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("hamza@test.com")
                .password("hashed").roles("USER").build();

        PlantSpeciesDto.SpeciesResponse species = PlantSpeciesDto.SpeciesResponse.builder()
                .id(1L).commonName("Peace Lily").scientificName("Spathiphyllum")
                .wateringFrequencyDays(7).lightRequirement("Low").toxicToCats(true)
                .createdAt(LocalDateTime.now()).build();

        plantResponse = UserPlantDto.PlantResponse.builder()
                .id(1L).nickname("Orchie").location("Living Room")
                .acquiredDate(LocalDate.now().minusMonths(1))
                .status("HEALTHY").createdAt(LocalDateTime.now()).species(species).build();
    }

    // ── GET /api/my-plants ────────────────────────────────────────────────

    /**
     * Test: getMyPlants() returns 200 OK with a page of the user's plants.
     */
    @Test
    @DisplayName("getMyPlants() - authenticated user returns 200 with plant page")
    void getMyPlants_authenticated_returns200WithPlantPage() {
        Page<UserPlantDto.PlantResponse> page = new PageImpl<>(List.of(plantResponse));
        when(plantService.getMyPlants(eq("hamza@test.com"), any())).thenReturn(page);

        ResponseEntity<ApiResponse<Page<UserPlantDto.PlantResponse>>> response =
                plantController.getMyPlants(userDetails, PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getContent().get(0).getNickname())
                .isEqualTo("Orchie");
    }

    /**
     * Test: getMyPlants() delegates to service with authenticated user's email.
     */
    @Test
    @DisplayName("getMyPlants() - delegates with authenticated user email")
    void getMyPlants_delegatesWithAuthEmail() {
        when(plantService.getMyPlants(anyString(), any())).thenReturn(Page.empty());

        plantController.getMyPlants(userDetails, PageRequest.of(0, 10));

        verify(plantService, times(1)).getMyPlants(eq("hamza@test.com"), any());
    }

    // ── GET /api/my-plants/due-today ──────────────────────────────────────

    /**
     * Test: getPlantsDueToday() returns 200 OK with list of plants needing care.
     */
    @Test
    @DisplayName("getPlantsDueToday() - returns 200 with plants due for care")
    void getPlantsDueToday_returns200WithDuePlants() {
        when(plantService.getPlantsDueToday("hamza@test.com"))
                .thenReturn(List.of(plantResponse));

        ResponseEntity<ApiResponse<List<UserPlantDto.PlantResponse>>> response =
                plantController.getPlantsDueToday(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getNickname()).isEqualTo("Orchie");
    }

    /**
     * Test: getPlantsDueToday() returns 200 with empty list when nothing is due.
     */
    @Test
    @DisplayName("getPlantsDueToday() - nothing due returns empty list")
    void getPlantsDueToday_nothingDue_returnsEmptyList() {
        when(plantService.getPlantsDueToday(anyString())).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<UserPlantDto.PlantResponse>>> response =
                plantController.getPlantsDueToday(userDetails);

        assertThat(response.getBody().getData()).isEmpty();
    }

    // ── GET /api/my-plants/{id} ───────────────────────────────────────────

    /**
     * Test: getPlantById() returns 200 OK with plant data for owned plant.
     */
    @Test
    @DisplayName("getPlantById() - owned plant returns 200 with plant data")
    void getPlantById_ownedPlant_returns200() {
        when(plantService.getPlantById("hamza@test.com", 1L)).thenReturn(plantResponse);

        ResponseEntity<ApiResponse<UserPlantDto.PlantResponse>> response =
                plantController.getPlantById(userDetails, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
    }

    /**
     * Test: getPlantById() propagates ResourceNotFoundException for unowned plant.
     */
    @Test
    @DisplayName("getPlantById() - unowned plant propagates ResourceNotFoundException")
    void getPlantById_unownedPlant_propagatesResourceNotFoundException() {
        when(plantService.getPlantById(anyString(), eq(99L)))
                .thenThrow(new ResourceNotFoundException("Plant not found with id: 99"));

        assertThatThrownBy(() -> plantController.getPlantById(userDetails, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── POST /api/my-plants ───────────────────────────────────────────────

    /**
     * Test: addPlant() returns 201 Created with the new plant.
     */
    @Test
    @DisplayName("addPlant() - valid request returns 201 Created")
    void addPlant_validRequest_returns201() {
        UserPlantDto.AddPlantRequest request = new UserPlantDto.AddPlantRequest();
        request.setSpeciesId(1L);
        request.setNickname("Kevin");

        when(plantService.addPlant(eq("hamza@test.com"), any())).thenReturn(plantResponse);

        ResponseEntity<ApiResponse<UserPlantDto.PlantResponse>> response =
                plantController.addPlant(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    /**
     * Test: addPlant() delegates to service with email from UserDetails.
     */
    @Test
    @DisplayName("addPlant() - delegates with email from UserDetails")
    void addPlant_delegatesWithUserDetailsEmail() {
        UserPlantDto.AddPlantRequest request = new UserPlantDto.AddPlantRequest();
        request.setSpeciesId(1L);
        request.setNickname("Kevin");
        when(plantService.addPlant(anyString(), any())).thenReturn(plantResponse);

        plantController.addPlant(userDetails, request);

        verify(plantService, times(1)).addPlant(eq("hamza@test.com"), any());
    }

    /**
     * Test: addPlant() propagates ResourceNotFoundException for unknown species.
     */
    @Test
    @DisplayName("addPlant() - unknown species propagates ResourceNotFoundException")
    void addPlant_unknownSpecies_propagatesResourceNotFoundException() {
        UserPlantDto.AddPlantRequest request = new UserPlantDto.AddPlantRequest();
        request.setSpeciesId(999L);
        request.setNickname("Kevin");

        when(plantService.addPlant(anyString(), any()))
                .thenThrow(new ResourceNotFoundException("Plant species", 999L));

        assertThatThrownBy(() -> plantController.addPlant(userDetails, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── PUT /api/my-plants/{id} ───────────────────────────────────────────

    /**
     * Test: updatePlant() returns 200 OK with updated plant.
     */
    @Test
    @DisplayName("updatePlant() - valid request returns 200 OK")
    void updatePlant_validRequest_returns200() {
        UserPlantDto.UpdatePlantRequest request = new UserPlantDto.UpdatePlantRequest();
        request.setNickname("Orchie Updated");
        request.setStatus(PlantStatus.STRUGGLING);

        when(plantService.updatePlant(anyString(), eq(1L), any())).thenReturn(plantResponse);

        ResponseEntity<ApiResponse<UserPlantDto.PlantResponse>> response =
                plantController.updatePlant(userDetails, 1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    // ── DELETE /api/my-plants/{id} ────────────────────────────────────────

    /**
     * Test: removePlant() returns 200 OK for an owned plant.
     */
    @Test
    @DisplayName("removePlant() - owned plant returns 200 OK")
    void removePlant_ownedPlant_returns200() {
        doNothing().when(plantService).removePlant("hamza@test.com", 1L);

        ResponseEntity<ApiResponse<Void>> response =
                plantController.removePlant(userDetails, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("removed");
    }

    /**
     * Test: removePlant() delegates to service with correct email and plant ID.
     */
    @Test
    @DisplayName("removePlant() - delegates with correct email and plant id")
    void removePlant_delegatesWithCorrectArgs() {
        doNothing().when(plantService).removePlant(anyString(), anyLong());

        plantController.removePlant(userDetails, 1L);

        verify(plantService, times(1)).removePlant("hamza@test.com", 1L);
    }

    /**
     * Test: removePlant() propagates ResourceNotFoundException for unowned plant.
     */
    @Test
    @DisplayName("removePlant() - unowned plant propagates ResourceNotFoundException")
    void removePlant_unownedPlant_propagatesResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("Plant not found with id: 99"))
                .when(plantService).removePlant(anyString(), eq(99L));

        assertThatThrownBy(() -> plantController.removePlant(userDetails, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
