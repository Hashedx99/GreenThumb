package com.greenthumb.feature.user;

import com.greenthumb.feature.care.CareLogRepository;
import com.greenthumb.feature.plant.PlantStatus;
import com.greenthumb.feature.plant.UserPlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service providing platform-level statistics for the admin dashboard.
 *
 * @author Hamza Ali
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final UserPlantRepository plantRepository;
    private final CareLogRepository careLogRepository;

    /**
     * Compiles and returns platform statistics for the admin dashboard.
     * <p>
     * Aggregates total users, total plants, care activity from the last 7 days,
     * top 5 most popular species, and a plant health status breakdown.
     * </p>
     *
     * @return the admin stats DTO with all aggregated metrics
     */
    public AdminStatsDto getStats() {
        // Total registered users
        long totalUsers = userRepository.count();

        // Total plants across all users
        long totalPlants = plantRepository.count();

        // Care logs from the past 7 days
        long careLogsThisWeek = careLogRepository.countLogsAfter(
                LocalDateTime.now().minusDays(7));

        // Top 5 species by plant count — raw query returns Object[]
        List<Map<String, Object>> topSpecies = plantRepository.findTopSpecies()
                .stream()
                .limit(5)
                .map(row -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("speciesId", row[0]);
                    entry.put("commonName", row[1]);
                    entry.put("plantCount", row[2]);
                    return entry;
                })
                .collect(Collectors.toList());

        // Plants grouped by health status
        Map<String, Long> plantsByStatus = new HashMap<>();
        Arrays.stream(PlantStatus.values()).forEach(status ->
                plantsByStatus.put(
                        status.name(),
                        plantRepository.findAll()
                                .stream()
                                .filter(p -> p.getStatus() == status)
                                .count()
                )
        );

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .totalPlants(totalPlants)
                .careLogsThisWeek(careLogsThisWeek)
                .topSpecies(topSpecies)
                .plantsByStatus(plantsByStatus)
                .build();
    }
}
