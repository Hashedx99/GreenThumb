package com.greenthumb.feature.care;

import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import com.greenthumb.shared.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
 * Unit tests for {@link CareController} using pure Mockito.
 * Controller methods are called directly — no Spring context loaded.
 *
 * @author Hamza Ali
 */
@ExtendWith(MockitoExtension.class)
class CareControllerTest {

    @Mock
    private CareService careService;

    @InjectMocks
    private CareController careController;

    private UserDetails userDetails;
    private CareDto.ScheduleResponse scheduleResponse;
    private CareDto.LogResponse logResponse;
    private CareDto.ScheduleRequest scheduleRequest;
    private CareDto.LogRequest logRequest;

    /**
     * Builds reusable fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("hamza@test.com")
                .password("hashed").roles("USER").build();

        scheduleResponse = CareDto.ScheduleResponse.builder()
                .id(1L).careType("WATER").intervalDays(7)
                .nextDueDate(LocalDate.now().plusDays(5)).isActive(true).build();

        logResponse = CareDto.LogResponse.builder()
                .id(1L).careType("WATER")
                .performedAt(LocalDateTime.now())
                .performedByName("Hamza Ali").build();

        scheduleRequest = new CareDto.ScheduleRequest();
        scheduleRequest.setCareType(CareType.WATER);
        scheduleRequest.setIntervalDays(7);
        scheduleRequest.setNextDueDate(LocalDate.now().plusDays(3));

        logRequest = new CareDto.LogRequest();
        logRequest.setCareType(CareType.WATER);
        logRequest.setNotes("Watered thoroughly");
    }

    // ── GET /api/my-plants/{id}/schedule ─────────────────────────────────

    /**
     * Test: getSchedules() returns 200 OK with list of schedules for the plant.
     */
    @Test
    @DisplayName("getSchedules() - owned plant returns 200 with schedules")
    void getSchedules_ownedPlant_returns200WithSchedules() {
        when(careService.getSchedules("hamza@test.com", 1L))
                .thenReturn(List.of(scheduleResponse));

        ResponseEntity<ApiResponse<List<CareDto.ScheduleResponse>>> response =
                careController.getSchedules(userDetails, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getCareType()).isEqualTo("WATER");
    }

    /**
     * Test: getSchedules() returns 200 with empty list when no schedules exist.
     */
    @Test
    @DisplayName("getSchedules() - no schedules returns empty list")
    void getSchedules_noSchedules_returnsEmptyList() {
        when(careService.getSchedules(anyString(), anyLong())).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<CareDto.ScheduleResponse>>> response =
                careController.getSchedules(userDetails, 1L);

        assertThat(response.getBody().getData()).isEmpty();
    }

    /**
     * Test: getSchedules() propagates ResourceNotFoundException for unowned plant.
     */
    @Test
    @DisplayName("getSchedules() - unowned plant propagates ResourceNotFoundException")
    void getSchedules_unownedPlant_propagatesResourceNotFoundException() {
        when(careService.getSchedules(anyString(), eq(99L)))
                .thenThrow(new ResourceNotFoundException("Plant not found with id: 99"));

        assertThatThrownBy(() -> careController.getSchedules(userDetails, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── POST /api/my-plants/{id}/schedule ────────────────────────────────

    /**
     * Test: createSchedule() returns 201 Created with the new schedule.
     */
    @Test
    @DisplayName("createSchedule() - valid request returns 201 Created")
    void createSchedule_validRequest_returns201() {
        when(careService.createSchedule(anyString(), anyLong(), any()))
                .thenReturn(scheduleResponse);

        ResponseEntity<ApiResponse<CareDto.ScheduleResponse>> response =
                careController.createSchedule(userDetails, 1L, scheduleRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getCareType()).isEqualTo("WATER");
        assertThat(response.getBody().getData().getIntervalDays()).isEqualTo(7);
    }

    /**
     * Test: createSchedule() propagates BusinessException for duplicate care type.
     */
    @Test
    @DisplayName("createSchedule() - duplicate care type propagates BusinessException")
    void createSchedule_duplicateCareType_propagatesBusinessException() {
        when(careService.createSchedule(anyString(), anyLong(), any()))
                .thenThrow(new BusinessException("A WATER schedule already exists for this plant."));

        assertThatThrownBy(() -> careController.createSchedule(userDetails, 1L, scheduleRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    /**
     * Test: createSchedule() delegates to service with correct args.
     */
    @Test
    @DisplayName("createSchedule() - delegates with correct email and plant id")
    void createSchedule_delegatesWithCorrectArgs() {
        when(careService.createSchedule(anyString(), anyLong(), any()))
                .thenReturn(scheduleResponse);

        careController.createSchedule(userDetails, 1L, scheduleRequest);

        verify(careService, times(1)).createSchedule("hamza@test.com", 1L, scheduleRequest);
    }

    // ── PUT /api/my-plants/{id}/schedule/{sid} ────────────────────────────

    /**
     * Test: updateSchedule() returns 200 OK with updated schedule.
     */
    @Test
    @DisplayName("updateSchedule() - valid request returns 200 OK")
    void updateSchedule_validRequest_returns200() {
        when(careService.updateSchedule(anyString(), anyLong(), anyLong(), any()))
                .thenReturn(scheduleResponse);

        ResponseEntity<ApiResponse<CareDto.ScheduleResponse>> response =
                careController.updateSchedule(userDetails, 1L, 1L, scheduleRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    // ── DELETE /api/my-plants/{id}/schedule/{sid} ─────────────────────────

    /**
     * Test: deleteSchedule() returns 200 OK for an owned schedule.
     */
    @Test
    @DisplayName("deleteSchedule() - owned schedule returns 200 OK")
    void deleteSchedule_ownedSchedule_returns200() {
        doNothing().when(careService).deleteSchedule("hamza@test.com", 1L, 1L);

        ResponseEntity<ApiResponse<Void>> response =
                careController.deleteSchedule(userDetails, 1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("removed");
    }

    /**
     * Test: deleteSchedule() delegates to service with correct args.
     */
    @Test
    @DisplayName("deleteSchedule() - delegates with correct email, plant id, schedule id")
    void deleteSchedule_delegatesWithCorrectArgs() {
        doNothing().when(careService).deleteSchedule(anyString(), anyLong(), anyLong());

        careController.deleteSchedule(userDetails, 2L, 5L);

        verify(careService, times(1)).deleteSchedule("hamza@test.com", 2L, 5L);
    }

    // ── POST /api/my-plants/{id}/care-log ────────────────────────────────

    /**
     * Test: logCare() returns 201 Created with the log entry.
     */
    @Test
    @DisplayName("logCare() - valid request returns 201 Created")
    void logCare_validRequest_returns201() {
        when(careService.logCare(anyString(), anyLong(), any()))
                .thenReturn(logResponse);

        ResponseEntity<ApiResponse<CareDto.LogResponse>> response =
                careController.logCare(userDetails, 1L, logRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getCareType()).isEqualTo("WATER");
        assertThat(response.getBody().getData().getPerformedByName()).isEqualTo("Hamza Ali");
    }

    /**
     * Test: logCare() delegates to service with correct args.
     */
    @Test
    @DisplayName("logCare() - delegates with correct email, plant id, and log request")
    void logCare_delegatesWithCorrectArgs() {
        when(careService.logCare(anyString(), anyLong(), any())).thenReturn(logResponse);

        careController.logCare(userDetails, 1L, logRequest);

        verify(careService, times(1)).logCare("hamza@test.com", 1L, logRequest);
    }

    /**
     * Test: logCare() propagates ResourceNotFoundException for unowned plant.
     */
    @Test
    @DisplayName("logCare() - unowned plant propagates ResourceNotFoundException")
    void logCare_unownedPlant_propagatesResourceNotFoundException() {
        when(careService.logCare(anyString(), eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Plant not found with id: 99"));

        assertThatThrownBy(() -> careController.logCare(userDetails, 99L, logRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── GET /api/my-plants/{id}/care-log ─────────────────────────────────

    /**
     * Test: getCareHistory() returns 200 OK with list of log entries.
     */
    @Test
    @DisplayName("getCareHistory() - owned plant returns 200 with history")
    void getCareHistory_ownedPlant_returns200WithHistory() {
        when(careService.getCareHistory("hamza@test.com", 1L))
                .thenReturn(List.of(logResponse));

        ResponseEntity<ApiResponse<List<CareDto.LogResponse>>> response =
                careController.getCareHistory(userDetails, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getCareType()).isEqualTo("WATER");
    }

    /**
     * Test: getCareHistory() returns empty list when no logs recorded.
     */
    @Test
    @DisplayName("getCareHistory() - no history returns empty list")
    void getCareHistory_noLogs_returnsEmptyList() {
        when(careService.getCareHistory(anyString(), anyLong())).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<CareDto.LogResponse>>> response =
                careController.getCareHistory(userDetails, 1L);

        assertThat(response.getBody().getData()).isEmpty();
    }
}
