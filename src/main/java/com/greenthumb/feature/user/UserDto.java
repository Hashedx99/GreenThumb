package com.greenthumb.feature.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Objects for User-related API requests and responses.
 *
 * @author Hamza Ali
 */
public class UserDto {

    // ── Requests ──────────────────────────────────────────────────────────

    /**
     * Request body for updating user profile details.
     */
    @Getter
    @Setter
    public static class UpdateProfileRequest {

        /** Updated display name. */
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        /** Short bio for the user's profile. */
        @Size(max = 300, message = "Bio must not exceed 300 characters")
        private String bio;

        /** User's city or country. */
        private String location;
    }

    /**
     * Request body for changing an authenticated user's password.
     */
    @Getter
    @Setter
    public static class ChangePasswordRequest {

        /** The user's current password for verification. */
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        /** The desired new password. */
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        private String newPassword;
    }

    // ── Responses ─────────────────────────────────────────────────────────

    /**
     * Response payload returned for user profile views.
     */
    @Getter
    @Builder
    public static class UserResponse {

        /** User's unique ID. */
        private Long id;

        /** Display name. */
        private String name;

        /** Email address. */
        private String email;

        /** Role (USER or ADMIN). */
        private String role;

        /** Account status. */
        private String status;

        /** URL of the user's profile picture. */
        private String profilePictureUrl;

        /** Short bio. */
        private String bio;

        /** Location. */
        private String location;

        /** Account creation timestamp. */
        private LocalDateTime createdAt;

        /**
         * Maps a {@link User} entity to a {@link UserResponse}.
         *
         * @param user the user entity to map
         * @return the corresponding UserResponse
         */
        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .status(user.getStatus().name())
                    .profilePictureUrl(user.getProfilePictureUrl())
                    .bio(user.getBio())
                    .location(user.getLocation())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    /**
     * Lightweight summary used in admin user list views.
     */
    @Getter
    @Builder
    public static class UserSummary {

        private Long id;
        private String name;
        private String email;
        private String role;
        private String status;
        private LocalDateTime createdAt;

        /**
         * Maps a {@link User} entity to a {@link UserSummary}.
         *
         * @param user the user entity to map
         * @return the corresponding UserSummary
         */
        public static UserSummary from(User user) {
            return UserSummary.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .status(user.getStatus().name())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }
}
