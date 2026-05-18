package com.greenthumb.feature.tips;

import com.greenthumb.feature.species.PlantSpecies;
import com.greenthumb.feature.species.PlantSpeciesRepository;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link TipsService} that fetches care tips from the
 * Perenual API and caches results in the {@link SpeciesTipsCache} table.
 *
 * @author Hamza Ali
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TipsServiceImpl implements TipsService {

    private final SpeciesTipsCacheRepository cacheRepository;
    private final PlantSpeciesRepository speciesRepository;
    private final RestTemplate restTemplate;

    @Value("${perenual.api.key}")
    private String perenualApiKey;

    @Value("${perenual.base.url}")
    private String perenualBaseUrl;

    /**
     * {@inheritDoc}
     * Serves from cache when entry is fresh (under 7 days).
     * Fetches from Perenual API when cache is absent or stale.
     */
    @Override
    @Transactional
    public TipsDto getTipsForSpecies(Long speciesId) {
        PlantSpecies species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new ResourceNotFoundException("Plant species", speciesId));

        // Serve from cache if present and not stale
        return cacheRepository.findBySpeciesId(speciesId)
                .filter(cache -> !cache.isStale())
                .map(cache -> buildDtoFromCache(cache, species, true))
                .orElseGet(() -> fetchFromPerenualAndCache(species));
    }

