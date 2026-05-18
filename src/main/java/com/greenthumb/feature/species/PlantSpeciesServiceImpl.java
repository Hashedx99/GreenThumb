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
