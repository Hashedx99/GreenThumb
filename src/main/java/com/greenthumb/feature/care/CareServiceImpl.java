package com.greenthumb.feature.care;

import com.greenthumb.feature.plant.UserPlant;
import com.greenthumb.feature.plant.UserPlantRepository;
import com.greenthumb.feature.user.User;
import com.greenthumb.feature.user.UserRepository;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CareService} for schedule and log management.
 *
 * @author Hamza Ali
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CareServiceImpl implements CareService {

    private final CareScheduleRepository scheduleRepository;
    private final CareLogRepository logRepository;
    private final UserPlantRepository plantRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CareDto.ScheduleResponse> getSchedules(String userEmail, Long plantId) {
        validatePlantOwnership(userEmail, plantId);
        return scheduleRepository.findByUserPlantId(plantId)
                .stream()
                .map(CareDto.ScheduleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Prevents duplicate schedule types for the same plant.
     */
    @Override
    @Transactional
    public CareDto.ScheduleResponse createSchedule(String userEmail, Long plantId,
                                                    CareDto.ScheduleRequest request) {
        UserPlant plant = findOwnedPlant(userEmail, plantId);

        // Prevent duplicate care types on the same plant
        scheduleRepository.findByUserPlantIdAndCareType(plantId, request.getCareType())
                .ifPresent(s -> {
                    throw new BusinessException(
                            "A " + request.getCareType().name() + " schedule already exists for this plant.");
                });

        CareSchedule schedule = CareSchedule.builder()
                .userPlant(plant)
                .careType(request.getCareType())
                .intervalDays(request.getIntervalDays())
                .nextDueDate(request.getNextDueDate())
                .isActive(true)
                .build();

        CareSchedule saved = scheduleRepository.save(schedule);
