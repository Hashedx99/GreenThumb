package com.greenthumb.feature.tips;

import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for species care tips responses.
 *
 * @author Hamza Ali
 */
@Getter
@Builder
public class TipsDto {

    /** The species ID these tips relate to. */
    private Long speciesId;

    /** The species common name. */
    private String speciesName;

    /** Watering guidance from Perenual. */
    private String wateringTip;

    /** Sunlight guidance from Perenual. */
    private String sunlightTip;

    /** Toxicity warning note. */
    private String toxicityNote;

    /** Whether this data was served from cache or freshly fetched. */
    private boolean fromCache;
}
