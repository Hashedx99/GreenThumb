package com.greenthumb.feature.care;

import java.util.List;

/**
 * Service interface for care schedule and care log operations.
 *
 * @author Hamza Ali
 */
public interface CareService {

    /**
     * Returns all care schedules for a given plant.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID
     * @return list of schedule responses
     */
    List<CareDto.ScheduleResponse> getSchedules(String userEmail, Long plantId);

    /**
     * Creates a new care schedule for a plant.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID
     * @param request   the schedule creation request
     * @return the created schedule response
     */
    CareDto.ScheduleResponse createSchedule(String userEmail, Long plantId,
                                             CareDto.ScheduleRequest request);

    /**
     * Updates an existing care schedule.
     *
     * @param userEmail  the authenticated user's email
     * @param plantId    the plant ID
     * @param scheduleId the schedule ID to update
     * @param request    the update request
     * @return the updated schedule response
     */
    CareDto.ScheduleResponse updateSchedule(String userEmail, Long plantId,
                                             Long scheduleId, CareDto.ScheduleRequest request);

    /**
     * Deletes (deactivates) a care schedule.
     *
     * @param userEmail  the authenticated user's email
     * @param plantId    the plant ID
     * @param scheduleId the schedule ID to delete
     */
    void deleteSchedule(String userEmail, Long plantId, Long scheduleId);

    /**
     * Logs a care action for a plant and advances the matching schedule's due date.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID
     * @param request   the care log request
     * @return the created log entry response
     */
    CareDto.LogResponse logCare(String userEmail, Long plantId, CareDto.LogRequest request);

    /**
     * Returns the care history for a plant, most recent first.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID
     * @return list of log responses
     */
    List<CareDto.LogResponse> getCareHistory(String userEmail, Long plantId);
}
