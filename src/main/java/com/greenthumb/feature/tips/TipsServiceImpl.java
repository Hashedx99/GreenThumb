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

    /**
     * Calls the Perenual API to fetch care guide data for the species,
     * saves the result to cache, and returns the tips DTO.
     *
     * @param species the PlantSpecies entity to fetch tips for
     * @return the freshly fetched TipsDto
     */
    @SuppressWarnings("unchecked")
    private TipsDto fetchFromPerenualAndCache(PlantSpecies species) {
        String wateringTip = "Water regularly based on soil moisture.";
        String sunlightTip = "Provide appropriate light for your species.";
        String toxicityNote = species.isToxicToCats()
                ? "⚠️ This plant is toxic to cats. Keep out of reach."
                : "This plant is generally considered safe for cats.";

        try {
            // Search Perenual for the species by scientific or common name
            String searchTerm = species.getScientificName() != null
                    ? species.getScientificName() : species.getCommonName();

            String searchUrl = perenualBaseUrl + "/species-list?key=" + perenualApiKey
                    + "&q=" + searchTerm.replace(" ", "%20");

            Map<String, Object> searchResult = restTemplate.getForObject(searchUrl, Map.class);

            if (searchResult != null && searchResult.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) searchResult.get("data");

                if (!data.isEmpty()) {
                    Integer perenualId = (Integer) data.get(0).get("id");

                    // Fetch the full care guide using the Perenual species ID
                    String careUrl = perenualBaseUrl + "/species-care-guide-list?key="
                            + perenualApiKey + "&species_id=" + perenualId;

                    Map<String, Object> careResult = restTemplate.getForObject(careUrl, Map.class);

                    if (careResult != null && careResult.containsKey("data")) {
                        List<Map<String, Object>> careData =
                                (List<Map<String, Object>>) careResult.get("data");

                        if (!careData.isEmpty()) {
                            List<Map<String, Object>> sections =
                                    (List<Map<String, Object>>) careData.get(0).get("section");

                            // Extract watering and sunlight sections from the API response
                            for (Map<String, Object> section : sections) {
                                String type = (String) section.get("type");
                                String desc = (String) section.get("description");
                                if ("watering".equalsIgnoreCase(type) && desc != null) {
                                    wateringTip = desc;
                                } else if ("sunlight".equalsIgnoreCase(type) && desc != null) {
                                    sunlightTip = desc;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log and fall back to defaults — don't fail the request for a tips enrichment
            log.warn("Failed to fetch tips from Perenual for species id={}: {}",
                    species.getId(), e.getMessage());
        }

        // Save or update the cache entry
        SpeciesTipsCache cacheEntry = cacheRepository.findBySpeciesId(species.getId())
                .orElse(SpeciesTipsCache.builder().species(species).build());

        cacheEntry.setWateringTip(wateringTip);
        cacheEntry.setSunlightTip(sunlightTip);
        cacheEntry.setToxicityNote(toxicityNote);
        cacheEntry.setFetchedAt(LocalDateTime.now());
        cacheRepository.save(cacheEntry);

        log.info("Tips fetched and cached for species id={}", species.getId());
        return buildDtoFromCache(cacheEntry, species, false);
    }

    /**
     * Builds a {@link TipsDto} from a cached entry.
     *
     * @param cache     the cache entity
     * @param species   the species entity
     * @param fromCache whether this was served from cache or freshly fetched
     * @return the TipsDto
     */
    private TipsDto buildDtoFromCache(SpeciesTipsCache cache, PlantSpecies species,
                                       boolean fromCache) {
        return TipsDto.builder()
                .speciesId(species.getId())
                .speciesName(species.getCommonName())
                .wateringTip(cache.getWateringTip())
                .sunlightTip(cache.getSunlightTip())
                .toxicityNote(cache.getToxicityNote())
                .fromCache(fromCache)
                .build();
    }
}
