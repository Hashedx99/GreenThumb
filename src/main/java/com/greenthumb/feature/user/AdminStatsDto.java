package com.greenthumb.feature.user;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for the admin platform statistics endpoint.
 *
 * @author Hamza Ali
 */
@Getter
@Builder
public class AdminStatsDto {

    /** Total number of registered users. */
    private long totalUsers;

    /** Total number of plants across all users. */
    private long totalPlants;

    /** Number of care log entries created in the last 7 days. */
    private long careLogsThisWeek;

    /** Top 5 most popular species by number of user plants. */
    private List<Map<String, Object>> topSpecies;

    /** Breakdown of plants by health status (HEALTHY, STRUGGLING, DEAD). */
    private Map<String, Long> plantsByStatus;
}
