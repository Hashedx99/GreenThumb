package com.greenthumb.feature.species;

import com.cloudinary.Cloudinary;
import com.greenthumb.feature.user.UserRepository;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Implementation of {@link PlantSpeciesService} for species catalogue management.
 *
 * @author Hamza Ali
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlantSpeciesServiceImpl implements PlantSpeciesService {

    private final PlantSpeciesRepository speciesRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    /**
     * {@inheritDoc}
     * Delegates to search query if provided, otherwise returns all species.
     */
    @Override
    public Page<PlantSpeciesDto.SpeciesResponse> getAllSpecies(String query, Pageable pageable) {
        if (StringUtils.hasText(query)) {
            // Use full-text search when a query is provided
            return speciesRepository.searchByName(query, pageable)
                    .map(PlantSpeciesDto.SpeciesResponse::from);
        }
        return speciesRepository.findAll(pageable)
                .map(PlantSpeciesDto.SpeciesResponse::from);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlantSpeciesDto.SpeciesResponse getSpeciesById(Long id) {
        PlantSpecies species = findSpeciesById(id);
        return PlantSpeciesDto.SpeciesResponse.from(species);
    }


    /**
     * Looks up a species by ID or throws a 404 exception.
     *
     * @param id the species ID
     * @return the PlantSpecies entity
     */
    private PlantSpecies findSpeciesById(Long id) {
        return speciesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plant species", id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PlantSpeciesDto.SpeciesResponse createSpecies(
            PlantSpeciesDto.SpeciesRequest request, String adminEmail) {

        var admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        PlantSpecies species = PlantSpecies.builder()
                .commonName(request.getCommonName())
                .scientificName(request.getScientificName())
                .wateringFrequencyDays(request.getWateringFrequencyDays())
                .lightRequirement(request.getLightRequirement())
                .toxicToCats(request.isToxicToCats())
                .createdByAdmin(admin)
                .build();

        PlantSpecies saved = speciesRepository.save(species);
        log.info("Species created: {} by admin: {}", saved.getCommonName(), adminEmail);
        return PlantSpeciesDto.SpeciesResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PlantSpeciesDto.SpeciesResponse updateSpecies(Long id, PlantSpeciesDto.SpeciesRequest request) {
        PlantSpecies species = findSpeciesById(id);

        // Update all mutable fields from the request
        species.setCommonName(request.getCommonName());
        species.setScientificName(request.getScientificName());
        species.setWateringFrequencyDays(request.getWateringFrequencyDays());
        species.setLightRequirement(request.getLightRequirement());
        species.setToxicToCats(request.isToxicToCats());

        PlantSpecies saved = speciesRepository.save(species);
        log.info("Species updated: id={}", id);
        return PlantSpeciesDto.SpeciesResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     * Uploads image to Cloudinary under the "greenthumb/species" folder.
     */
    @Override
    @Transactional
    public PlantSpeciesDto.SpeciesResponse uploadSpeciesImage(Long id, MultipartFile file) {
        PlantSpecies species = findSpeciesById(id);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "greenthumb/species")
            );
            species.setImageUrl((String) result.get("secure_url"));
            PlantSpecies saved = speciesRepository.save(species);
            log.info("Species image uploaded: id={}", id);
            return PlantSpeciesDto.SpeciesResponse.from(saved);
        } catch (IOException e) {
            log.error("Failed to upload species image for id={}", id, e);
            throw new BusinessException("Failed to upload species image. Please try again.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteSpecies(Long id) {
        PlantSpecies species = findSpeciesById(id);
        speciesRepository.delete(species);
        log.info("Species deleted: id={}", id);
    }

}
