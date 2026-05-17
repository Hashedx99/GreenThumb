package com.greenthumb.feature.auth;

/**
 * Service interface for authentication and account lifecycle operations.
 *
 * @author Hamza Ali
 */
public interface AuthService {

    /**
     * Registers a new user, saves them with PENDING status,
     * and sends a verification email.
     *
     * @param request the registration request containing name, email, and password
     */
    void register(AuthDto.RegisterRequest request);

    /**
     * Verifies a user's email using the token from the verification link.
     * Sets the user's status to ACTIVE and emailVerified to true.
     *
     * @param token the verification token string
     */
    void verifyEmail(String token);

    /**
     * Authenticates a user and returns a JWT token on success.
     *
     * @param request the login request containing email and password
     * @return login response containing JWT and user details
     */
    AuthDto.LoginResponse login(AuthDto.LoginRequest request);

    /**
     * Initiates the forgot password flow by generating a reset token
     * and sending a reset link to the user's email.
     *
     * @param request the forgot password request containing the user's email
     */
    void forgotPassword(AuthDto.ForgotPasswordRequest request);

    /**
     * Completes the password reset by validating the token
     * and updating the user's password.
     *
     * @param request the reset request containing token and new password
     */
    void resetPassword(AuthDto.ResetPasswordRequest request);
}
