package com.greenthumb.feature.care;

import com.greenthumb.feature.plant.UserPlant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing a recurring care schedule for a user's plant.
 * <p>
 * Each schedule defines a type of care (e.g. WATER), how often it should
 * occur (intervalDays), and when it is next due. Logging a care action
 * automatically advances {@code nextDueDate} by {@code intervalDays}.
 * </p>
 *
 * @author Hamza Ali
 */
@Entity
@Table(name = "care_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareSchedule {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The plant this schedule applies to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_plant_id", nullable = false)
    private UserPlant userPlant;

    /** The type of care this schedule tracks. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareType careType;

    /** Number of days between each care action. */
    @Column(nullable = false)
    private Integer intervalDays;

    /** The next date this care action is due. */
    @Column(nullable = false)
    private LocalDate nextDueDate;

    /** Whether this schedule is still active. */
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
