package com.greenthumb.feature.tips;

import com.greenthumb.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Perenual-powered care tips endpoint.
 *
 * @author Hamza Ali
 */
@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class TipsController {

    private final TipsService tipsService;

    /**
     * Returns care tips for a given species, sourced from the Perenual API
     * and cached locally to avoid repeated external calls.
     *
     * @param speciesId the species ID to retrieve tips for
     * @return 200 with tips data including watering, sunlight, and toxicity info
     */
    @GetMapping("/{speciesId}/tips")
    public ResponseEntity<ApiResponse<TipsDto>> getTipsForSpecies(
            @PathVariable Long speciesId) {
        TipsDto tips = tipsService.getTipsForSpecies(speciesId);
        return ResponseEntity.ok(ApiResponse.success("Care tips retrieved", tips));
    }
}
