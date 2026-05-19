package com.greenthumb.feature.care;

import com.greenthumb.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for plant care schedule and care log endpoints.
 * All routes require authentication (JWT).
 *
 * @author Hamza Ali
 */
@RestController
@RequestMapping("/api/my-plants/{plantId}")
@RequiredArgsConstructor
public class CareController {

    private final CareService careService;

    // ── Schedule endpoints ────────────────────────────────────────────────

    /**
     * Returns all care schedules for a given plant.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID
     * @return 200 with list of schedules
     */
    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<CareDto.ScheduleResponse>>> getSchedules(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId) {
        List<CareDto.ScheduleResponse> schedules =
                careService.getSchedules(userDetails.getUsername(), plantId);
        return ResponseEntity.ok(ApiResponse.success("Care schedules retrieved", schedules));
    }

    /**
     * Creates a new care schedule for the given plant.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID
     * @param request     the schedule creation request body
     * @return 201 Created with the new schedule
     */
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<CareDto.ScheduleResponse>> createSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @Valid @RequestBody CareDto.ScheduleRequest request) {
        CareDto.ScheduleResponse schedule =
                careService.createSchedule(userDetails.getUsername(), plantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Care schedule created", schedule));
    }

    /**
     * Updates an existing care schedule.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID
     * @param scheduleId  the schedule ID to update
     * @param request     the update request body
     * @return 200 with updated schedule
     */
    @PutMapping("/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<CareDto.ScheduleResponse>> updateSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody CareDto.ScheduleRequest request) {
        CareDto.ScheduleResponse schedule =
                careService.updateSchedule(userDetails.getUsername(), plantId, scheduleId, request);
        return ResponseEntity.ok(ApiResponse.success("Care schedule updated", schedule));
    }

    /**
     * Deactivates a care schedule (soft delete).
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID
     * @param scheduleId  the schedule ID to deactivate
     * @return 200 success message
     */
    @DeleteMapping("/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @PathVariable Long scheduleId) {
        careService.deleteSchedule(userDetails.getUsername(), plantId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success("Care schedule removed"));
    }

    // ── Care log endpoints ────────────────────────────────────────────────

    /**
     * Logs a care action performed on the given plant.
     * Automatically advances the matching schedule's next due date.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID
     * @param request     the care log request body
     * @return 201 Created with the log entry
     */
    @PostMapping("/care-log")
    public ResponseEntity<ApiResponse<CareDto.LogResponse>> logCare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @Valid @RequestBody CareDto.LogRequest request) {
        CareDto.LogResponse log = careService.logCare(userDetails.getUsername(), plantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Care logged successfully", log));
    }

    /**
     * Returns the full care history for a given plant, most recent first.
     *
     * @param userDetails the authenticated user
     * @param plantId     the plant ID
     * @return 200 with list of care log entries
     */
    @GetMapping("/care-log")
    public ResponseEntity<ApiResponse<List<CareDto.LogResponse>>> getCareHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId) {
        List<CareDto.LogResponse> history =
                careService.getCareHistory(userDetails.getUsername(), plantId);
        return ResponseEntity.ok(ApiResponse.success("Care history retrieved", history));
    }
}
