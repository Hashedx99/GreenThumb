package com.greenthumb.feature.care;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link CareLog} persistence operations.
 *
 * @author Hamza Ali
 */
@Repository
public interface CareLogRepository extends JpaRepository<CareLog, Long> {

    /**
     * Returns all care logs for a specific plant, ordered most recent first.
     *
     * @param userPlantId the plant ID
     * @return list of care logs
     */
    List<CareLog> findByUserPlantIdOrderByPerformedAtDesc(Long userPlantId);

    /**
     * Counts care logs created after a given timestamp — used for admin stats.
     *
     * @param since the start of the time window
     * @return number of care log entries since that timestamp
     */
    @Query("SELECT COUNT(cl) FROM CareLog cl WHERE cl.performedAt >= :since")
    long countLogsAfter(@Param("since") LocalDateTime since);
}
