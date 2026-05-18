package com.greenthumb.feature.tips;

/**
 * Service interface for fetching and caching plant care tips
 * from the Perenual external API.
 *
 * @author Hamza Ali
 */
public interface TipsService {

    /**
     * Returns care tips for a given species.
     * <p>
     * Serves from cache if a fresh entry exists (less than 7 days old).
     * Otherwise calls the Perenual API, stores the result, and returns it.
     * </p>
     *
     * @param speciesId the species ID to fetch tips for
     * @return the tips DTO with watering, sunlight, and toxicity data
     */
    TipsDto getTipsForSpecies(Long speciesId);
}
