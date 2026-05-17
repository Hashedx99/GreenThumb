package com.greenthumb.feature.auth;

import com.greenthumb.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing all public authentication endpoints.
 * <p>
 * All routes under {@code /auth/**} are publicly accessible
 * (no JWT required) as configured in {@link com.greenthumb.shared.config.SecurityConfig}.
 * </p>
 *
 * @author Hamza Ali
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user and sends an email verification link.
     *
     * @param request the registration request body
     * @return 201 Created with success message
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please check your email to verify your account."));
    }

    /**
     * Verifies a user's email address using the token from the email link.
     *
     * @param token the verification token query parameter
     * @return 200 if verified, or error if token is invalid/expired
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now log in."));
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login request body
     * @return 200 with JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        AuthDto.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Initiates the forgot password flow — sends a reset link to the given email.
     *
     * @param request the forgot password request body
     * @return 200 always (prevents user enumeration)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody AuthDto.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("If that email is registered, a reset link has been sent."));
    }

    /**
     * Resets a user's password using the token from the reset email.
     *
     * @param request the reset password request body
     * @return 200 on success
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody AuthDto.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. You can now log in."));
    }
}
