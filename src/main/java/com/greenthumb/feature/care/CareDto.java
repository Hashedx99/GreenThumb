package com.greenthumb.feature.care;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Objects for care schedule and care log API interactions.
 *
 * @author Hamza Ali
 */
public class CareDto {

    // ── Requests ──────────────────────────────────────────────────────────

    /**
     * Request body for creating or updating a care schedule.
     */
    @Getter
    @Setter
    public static class ScheduleRequest {

        /** The type of care this schedule tracks. */
        @NotNull(message = "Care type is required")
        private CareType careType;

        /** Number of days between care sessions — must be at least 1. */
        @NotNull(message = "Interval days is required")
        @Positive(message = "Interval must be a positive number of days")
        private Integer intervalDays;

        /** The date the first care action is due. */
        @NotNull(message = "Next due date is required")
        private LocalDate nextDueDate;
    }

    /**
     * Request body for logging a care action performed on a plant.
     */
    @Getter
    @Setter
    public static class LogRequest {

        /** The care type that was performed. */
        @NotNull(message = "Care type is required")
        private CareType careType;

        /** Optional notes about this care session. */
        private String notes;
    }

    // ── Responses ─────────────────────────────────────────────────────────

    /**
     * Response payload for a care schedule entry.
     */
    @Getter
    @Builder
    public static class ScheduleResponse {

        private Long id;
        private String careType;
        private Integer intervalDays;
        private LocalDate nextDueDate;
        private boolean isActive;

        /**
         * Maps a {@link CareSchedule} entity to a {@link ScheduleResponse}.
         *
         * @param schedule the entity to map
         * @return the corresponding ScheduleResponse
         */
        public static ScheduleResponse from(CareSchedule schedule) {
            return ScheduleResponse.builder()
                    .id(schedule.getId())
                    .careType(schedule.getCareType().name())
                    .intervalDays(schedule.getIntervalDays())
                    .nextDueDate(schedule.getNextDueDate())
                    .isActive(schedule.isActive())
                    .build();
        }
    }

    /**
     * Response payload for a care log entry.
     */
    @Getter
    @Builder
    public static class LogResponse {

        private Long id;
        private String careType;
        private LocalDateTime performedAt;
        private String notes;
        private String performedByName;

        /**
         * Maps a {@link CareLog} entity to a {@link LogResponse}.
         *
         * @param log the entity to map
         * @return the corresponding LogResponse
         */
        public static LogResponse from(CareLog log) {
            return LogResponse.builder()
                    .id(log.getId())
                    .careType(log.getCareType().name())
                    .performedAt(log.getPerformedAt())
                    .notes(log.getNotes())
                    .performedByName(log.getPerformedBy().getName())
                    .build();
        }
    }
}
