package com.greenthumb.feature.user;

import com.greenthumb.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for user profile and admin user management endpoints.
 *
 * @author Hamza Ali
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Profile endpoints (Private) ───────────────────────────────────────

    /**
     * Returns the authenticated user's own profile.
     *
     * @param userDetails the authenticated user injected by Spring Security
     * @return 200 with the user's profile data
     */
    @GetMapping("/api/users/profile")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto.UserResponse response = userService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", response));
    }

    /**
     * Updates the authenticated user's name, bio, and location.
     *
     * @param userDetails the authenticated user
     * @param request     the update request body
     * @return 200 with updated profile
     */
    @PutMapping("/api/users/profile")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto.UpdateProfileRequest request) {
        UserDto.UserResponse response = userService.updateMyProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    /**
     * Uploads a new profile picture for the authenticated user.
     *
     * @param userDetails the authenticated user
     * @param file        the image file (multipart)
     * @return 200 with updated profile including new picture URL
     */
    @PostMapping("/api/users/profile/picture")
    public ResponseEntity<ApiResponse<UserDto.UserResponse>> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        UserDto.UserResponse response = userService.uploadProfilePicture(userDetails.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.success("Profile picture updated", response));
    }

    /**
     * Changes the authenticated user's password.
     *
     * @param userDetails the authenticated user
     * @param request     the change password request body
     * @return 200 success message
     */
    @PostMapping("/api/users/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto.ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    // ── Admin endpoints ───────────────────────────────────────────────────

    /**
     * Returns a paginated list of all users — admin only.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return 200 with page of user summaries
     */
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDto.UserSummary>>> getAllUsers(Pageable pageable) {
        Page<UserDto.UserSummary> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    /**
     * Soft-deletes a user by ID — sets status to INACTIVE — admin only.
     *
     * @param userId the ID of the user to deactivate
     * @return 200 success message
     */
    @DeleteMapping("/api/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteUser(@PathVariable Long userId) {
        userService.softDeleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }
}
