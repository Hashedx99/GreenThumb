package com.greenthumb.feature.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Objects for authentication-related API requests and responses.
 *
 * @author Hamza Ali
 */
public class AuthDto {

    // ── Requests ──────────────────────────────────────────────────────────

    /**
     * Request body for user registration.
     */
    @Getter
    @Setter
    public static class RegisterRequest {

        /** The user's display name. */
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        /** The user's email address — must be unique. */
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        /** The desired password — minimum 8 characters. */
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
    }

    /**
     * Request body for user login.
     */
    @Getter
    @Setter
    public static class LoginRequest {

        /** The user's registered email. */
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        /** The user's password. */
        @NotBlank(message = "Password is required")
        private String password;
    }

    /**
     * Request body to initiate the forgot password flow.
     */
    @Getter
    @Setter
    public static class ForgotPasswordRequest {

        /** The email address associated with the account. */
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
    }

    /**
     * Request body to complete the password reset using a token.
     */
    @Getter
    @Setter
    public static class ResetPasswordRequest {

        /** The reset token received via email. */
        @NotBlank(message = "Token is required")
        private String token;

        /** The new password to set. */
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String newPassword;
    }

    // ── Responses ─────────────────────────────────────────────────────────

    /**
     * Response payload returned upon successful login.
     */
    @Getter
    @Builder
    public static class LoginResponse {

        /** The JWT token to include in subsequent requests. */
        private String token;

        /** The authenticated user's ID. */
        private Long userId;

        /** The authenticated user's display name. */
        private String name;

        /** The authenticated user's email. */
        private String email;

        /** The authenticated user's role. */
        private String role;
    }
}
