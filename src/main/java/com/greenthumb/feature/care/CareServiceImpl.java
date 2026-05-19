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
        log.info("Care schedule created: {} for plant id={}", request.getCareType(), plantId);
        return CareDto.ScheduleResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CareDto.ScheduleResponse updateSchedule(String userEmail, Long plantId,
                                                    Long scheduleId,
                                                    CareDto.ScheduleRequest request) {
        validatePlantOwnership(userEmail, plantId);
        CareSchedule schedule = scheduleRepository.findById(scheduleId)
                .filter(s -> s.getUserPlant().getId().equals(plantId))
                .orElseThrow(() -> new ResourceNotFoundException("Care schedule", scheduleId));

        schedule.setIntervalDays(request.getIntervalDays());
        schedule.setNextDueDate(request.getNextDueDate());

        CareSchedule saved = scheduleRepository.save(schedule);
        log.info("Care schedule updated: id={}", scheduleId);
        return CareDto.ScheduleResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     * Sets isActive=false rather than deleting the record.
     */
    @Override
    @Transactional
    public void deleteSchedule(String userEmail, Long plantId, Long scheduleId) {
        validatePlantOwnership(userEmail, plantId);
        CareSchedule schedule = scheduleRepository.findById(scheduleId)
                .filter(s -> s.getUserPlant().getId().equals(plantId))
                .orElseThrow(() -> new ResourceNotFoundException("Care schedule", scheduleId));

        // Deactivate rather than hard delete to preserve history
        schedule.setActive(false);
        scheduleRepository.save(schedule);
        log.info("Care schedule deactivated: id={}", scheduleId);
    }

    /**
     * {@inheritDoc}
     * Advances the matching schedule's nextDueDate after logging.
     */
    @Override
    @Transactional
    public CareDto.LogResponse logCare(String userEmail, Long plantId, CareDto.LogRequest request) {
        UserPlant plant = findOwnedPlant(userEmail, plantId);
        User user = findUserByEmail(userEmail);

        CareLog careLog = CareLog.builder()
                .userPlant(plant)
                .performedBy(user)
                .careType(request.getCareType())
                .notes(request.getNotes())
                .build();

        CareLog saved = logRepository.save(careLog);

        // Advance the next due date on the matching active schedule
        scheduleRepository.findByUserPlantIdAndCareType(plantId, request.getCareType())
                .filter(CareSchedule::isActive)
                .ifPresent(schedule -> {
                    LocalDate next = LocalDate.now().plusDays(schedule.getIntervalDays());
                    schedule.setNextDueDate(next);
                    scheduleRepository.save(schedule);
                    log.debug("Schedule advanced: next due={}", next);
                });

        log.info("Care logged: {} for plant id={}", request.getCareType(), plantId);
        return CareDto.LogResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CareDto.LogResponse> getCareHistory(String userEmail, Long plantId) {
        validatePlantOwnership(userEmail, plantId);
        return logRepository.findByUserPlantIdOrderByPerformedAtDesc(plantId)
                .stream()
                .map(CareDto.LogResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Validates that the given plant exists and belongs to the requesting user.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID to check
     */
    private void validatePlantOwnership(String userEmail, Long plantId) {
        findOwnedPlant(userEmail, plantId);
    }

    /**
     * Returns the plant if owned by the user, or throws 404.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID
     * @return the UserPlant entity
     */
    private UserPlant findOwnedPlant(String userEmail, Long plantId) {
        User user = findUserByEmail(userEmail);
        return plantRepository.findByIdAndUserId(plantId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plant not found with id: " + plantId));
    }

    /**
     * Looks up a user by email or throws 404.
     *
     * @param email the user's email
     * @return the User entity
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
