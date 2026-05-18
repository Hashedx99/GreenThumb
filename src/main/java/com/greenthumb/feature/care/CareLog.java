package com.greenthumb.feature.care;

import com.greenthumb.feature.plant.UserPlant;
import com.greenthumb.feature.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity recording a single care action performed on a plant.
 * <p>
 * Each log entry captures what was done, when, and by whom.
 * Logging a care action also advances the corresponding
 * {@link CareSchedule}'s nextDueDate.
 * </p>
 *
 * @author Hamza Ali
 */
@Entity
@Table(name = "care_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareLog {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The plant this care action was performed on. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_plant_id", nullable = false)
    private UserPlant userPlant;

    /** The user who performed the care action. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    /** The type of care that was performed. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareType careType;

    /** When the care action was performed. */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime performedAt = LocalDateTime.now();

    /** Optional notes about this care session. */
    @Column(length = 500)
    private String notes;
}
