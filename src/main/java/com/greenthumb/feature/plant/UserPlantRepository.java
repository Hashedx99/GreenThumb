package com.greenthumb.feature.plant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link UserPlant} persistence operations.
 *
 * @author Hamza Ali
 */
@Repository
public interface UserPlantRepository extends JpaRepository<UserPlant, Long> {

    /**
     * Returns all plants owned by a specific user with pagination.
     *
     * @param userId   the owner's user ID
     * @param pageable pagination parameters
     * @return a page of the user's plants
     */
    Page<UserPlant> findByUserId(Long userId, Pageable pageable);

    /**
     * Finds a specific plant by ID and owner — used for ownership validation.
     *
     * @param id     the plant ID
     * @param userId the owner's user ID
     * @return Optional containing the plant if it belongs to the user
     */
    Optional<UserPlant> findByIdAndUserId(Long id, Long userId);

    /**
     * Returns plants that have care schedules due on or before today.
     * Used for the "due-today" reminder feature.
     *
     * @param userId  the owner's user ID
     * @param today   the current date
     * @return list of plants needing care
     */
    @Query("SELECT DISTINCT up FROM CareSchedule cs " +
           "JOIN cs.userPlant up " +
           "WHERE up.user.id = :userId " +
           "AND cs.isActive = true " +
           "AND cs.nextDueDate <= :today")
    List<UserPlant> findPlantsDueForCare(@Param("userId") Long userId,
                                          @Param("today") LocalDate today);

    /**
     * Counts the total number of plants across all users — used for admin stats.
     *
     * @return total plant count
     */
    long count();

    /**
     * Returns the top 5 most popular species by number of user plants.
     *
     * @return list of [speciesId, commonName, count] projections
     */
    @Query("SELECT up.species.id, up.species.commonName, COUNT(up) as cnt " +
           "FROM UserPlant up GROUP BY up.species.id, up.species.commonName " +
           "ORDER BY cnt DESC")
    List<Object[]> findTopSpecies();
}
