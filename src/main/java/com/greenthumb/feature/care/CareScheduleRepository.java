package com.greenthumb.feature.care;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link CareSchedule} persistence operations.
 *
 * @author Hamza Ali
 */
@Repository
public interface CareScheduleRepository extends JpaRepository<CareSchedule, Long> {

    /**
     * Finds all care schedules for a specific plant.
     *
     * @param userPlantId the plant ID
     * @return list of schedules for that plant
     */
    List<CareSchedule> findByUserPlantId(Long userPlantId);

    /**
     * Finds a specific care schedule by plant and care type.
     * Used when logging care to advance the nextDueDate.
     *
     * @param userPlantId the plant ID
     * @param careType    the type of care
     * @return Optional schedule if it exists
     */
    Optional<CareSchedule> findByUserPlantIdAndCareType(Long userPlantId, CareType careType);

    /**
     * Counts total care logs created this week — used in admin stats.
     *
     * @return count of active schedules across all plants
     */
    long countByIsActiveTrue();
}
