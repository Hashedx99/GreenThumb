package com.greenthumb.feature.tips;

import com.greenthumb.feature.species.PlantSpecies;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity caching care tips fetched from the Perenual external API.
 * <p>
 * Results are cached to avoid repeated external API calls.
 * The cache is considered stale after 7 days.
 * </p>
 *
 * @author Hamza Ali
 */
@Entity
@Table(name = "species_tips_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeciesTipsCache {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The species these tips relate to — one-to-one. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false, unique = true)
    private PlantSpecies species;

    /** Watering care tip from Perenual. */
    @Column(length = 1000)
    private String wateringTip;

    /** Sunlight care tip from Perenual. */
    @Column(length = 1000)
    private String sunlightTip;

    /** Toxicity note from Perenual. */
    @Column(length = 500)
    private String toxicityNote;

    /** When this cache entry was last fetched from the API. */
    @Column(nullable = false)
    private LocalDateTime fetchedAt;

    /**
     * Checks whether this cache entry is older than 7 days.
     *
     * @return true if the cached data is stale
     */
    public boolean isStale() {
        return fetchedAt.isBefore(LocalDateTime.now().minusDays(7));
    }
}
