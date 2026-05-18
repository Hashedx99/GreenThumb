package com.greenthumb.feature.plant;

import com.cloudinary.Cloudinary;
import com.greenthumb.feature.species.PlantSpecies;
import com.greenthumb.feature.species.PlantSpeciesRepository;
import com.greenthumb.feature.user.User;
import com.greenthumb.feature.user.UserRepository;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link UserPlantService} for plant collection management.
 *
 * @author Hamza Ali
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPlantServiceImpl implements UserPlantService {

    private final UserPlantRepository plantRepository;
    private final UserRepository userRepository;
    private final PlantSpeciesRepository speciesRepository;
    private final Cloudinary cloudinary;

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<UserPlantDto.PlantResponse> getMyPlants(String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        return plantRepository.findByUserId(user.getId(), pageable)
                .map(UserPlantDto.PlantResponse::from);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserPlantDto.PlantResponse getPlantById(String userEmail, Long plantId) {
        UserPlant plant = findOwnedPlant(userEmail, plantId);
        return UserPlantDto.PlantResponse.from(plant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserPlantDto.PlantResponse addPlant(String userEmail, UserPlantDto.AddPlantRequest request) {
        User user = findUserByEmail(userEmail);
        PlantSpecies species = speciesRepository.findById(request.getSpeciesId())
                .orElseThrow(() -> new ResourceNotFoundException("Plant species", request.getSpeciesId()));

        UserPlant plant = UserPlant.builder()
                .user(user)
                .species(species)
                .nickname(request.getNickname())
                .acquiredDate(request.getAcquiredDate())
                .location(request.getLocation())
                .notes(request.getNotes())
                .status(PlantStatus.HEALTHY)
                .build();

        UserPlant saved = plantRepository.save(plant);
        log.info("Plant added to collection: '{}' for user: {}", saved.getNickname(), userEmail);
        return UserPlantDto.PlantResponse.from(saved);
    }

    /**
     * Looks up a user by email or throws a 404.
     *
     * @param email the user's email
     * @return the User entity
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserPlantDto.PlantResponse updatePlant(String userEmail, Long plantId,
                                                   UserPlantDto.UpdatePlantRequest request) {
        UserPlant plant = findOwnedPlant(userEmail, plantId);

        plant.setNickname(request.getNickname());
        plant.setLocation(request.getLocation());
        plant.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            plant.setStatus(request.getStatus());
        }

        UserPlant saved = plantRepository.save(plant);
        log.info("Plant updated: id={}", plantId);
        return UserPlantDto.PlantResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     * Uploads to Cloudinary under "greenthumb/plants" folder.
     */
    @Override
    @Transactional
    public UserPlantDto.PlantResponse uploadPlantPhoto(String userEmail, Long plantId,
                                                        MultipartFile file) {
        UserPlant plant = findOwnedPlant(userEmail, plantId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "greenthumb/plants")
            );
            plant.setPhotoUrl((String) result.get("secure_url"));
            UserPlant saved = plantRepository.save(plant);
            log.info("Plant photo uploaded: id={}", plantId);
            return UserPlantDto.PlantResponse.from(saved);
        } catch (IOException e) {
            log.error("Failed to upload plant photo: id={}", plantId, e);
            throw new BusinessException("Failed to upload plant photo. Please try again.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void removePlant(String userEmail, Long plantId) {
        UserPlant plant = findOwnedPlant(userEmail, plantId);
        plantRepository.delete(plant);
        log.info("Plant removed from collection: id={}", plantId);
    }

    /**
     * {@inheritDoc}
     * Queries care schedules with nextDueDate on or before today.
     */
    @Override
    public List<UserPlantDto.PlantResponse> getPlantsDueToday(String userEmail) {
        User user = findUserByEmail(userEmail);
        return plantRepository.findPlantsDueForCare(user.getId(), LocalDate.now())
                .stream()
                .map(UserPlantDto.PlantResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Finds a plant by ID and validates it belongs to the requesting user.
     *
     * @param userEmail the authenticated user's email
     * @param plantId   the plant ID
     * @return the plant if it belongs to the user
     * @throws ResourceNotFoundException if not found
     * @throws BusinessException         if the plant belongs to another user
     */
    private UserPlant findOwnedPlant(String userEmail, Long plantId) {
        User user = findUserByEmail(userEmail);
        return plantRepository.findByIdAndUserId(plantId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plant not found with id: " + plantId));
    }
}
