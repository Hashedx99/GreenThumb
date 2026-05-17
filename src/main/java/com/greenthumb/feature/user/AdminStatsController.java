package com.greenthumb.feature.user;

import com.greenthumb.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin-only platform statistics.
 *
 * @author Hamza Ali
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService statsService;

    /**
     * Returns aggregated platform statistics — admin only.
     * <p>
     * Includes total users, total plants, care logs this week,
     * top species, and plant health status breakdown.
     * </p>
     *
     * @return 200 with the stats payload
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getStats() {
        AdminStatsDto stats = statsService.getStats();
        return ResponseEntity.ok(ApiResponse.success("Platform stats retrieved", stats));
    }
}
