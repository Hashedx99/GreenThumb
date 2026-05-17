package com.greenthumb.feature.auth;

import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.DuplicateResourceException;
import com.greenthumb.shared.exception.InvalidTokenException;
import com.greenthumb.shared.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthController} using pure Mockito.
 * Controller methods are called directly — no Spring context loaded.
 *
 * @author Hamza Ali
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthDto.RegisterRequest registerRequest;
    private AuthDto.LoginRequest loginRequest;
    private AuthDto.ForgotPasswordRequest forgotRequest;
    private AuthDto.ResetPasswordRequest resetRequest;

    /**
     * Builds reusable request fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        registerRequest = new AuthDto.RegisterRequest();
        registerRequest.setName("Hamza Ali");
        registerRequest.setEmail("hamza@test.com");
        registerRequest.setPassword("SecurePass1!");

        loginRequest = new AuthDto.LoginRequest();
        loginRequest.setEmail("hamza@test.com");
        loginRequest.setPassword("SecurePass1!");

        forgotRequest = new AuthDto.ForgotPasswordRequest();
        forgotRequest.setEmail("hamza@test.com");

        resetRequest = new AuthDto.ResetPasswordRequest();
        resetRequest.setToken("valid-reset-token");
        resetRequest.setNewPassword("NewPass@123");
    }

    // ── register ─────────────────────────────────────────────────────────

    /**
     * Test: register() returns 201 Created on valid input.
     */
    @Test
    @DisplayName("register() - valid request returns 201 Created")
    void register_validRequest_returns201() {
        doNothing().when(authService).register(any());

        ResponseEntity<ApiResponse<Void>> response = authController.register(registerRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    /**
     * Test: register() calls authService exactly once with the correct request.
     */
    @Test
    @DisplayName("register() - delegates to authService exactly once")
    void register_delegatesToAuthServiceOnce() {
        doNothing().when(authService).register(any());

        authController.register(registerRequest);

        verify(authService, times(1)).register(registerRequest);
        verifyNoMoreInteractions(authService);
    }

    /**
     * Test: register() propagates DuplicateResourceException for duplicate email.
     */
    @Test
    @DisplayName("register() - duplicate email propagates DuplicateResourceException")
    void register_duplicateEmail_propagatesDuplicateResourceException() {
        doThrow(new DuplicateResourceException("Email already registered"))
                .when(authService).register(any());

        assertThatThrownBy(() -> authController.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");
    }

    /**
     * Test: register() response message tells user to check their email.
     */
    @Test
    @DisplayName("register() - response message instructs email check")
    void register_validRequest_responseMessageInstructsEmailCheck() {
        doNothing().when(authService).register(any());

        ResponseEntity<ApiResponse<Void>> response = authController.register(registerRequest);

        assertThat(response.getBody().getMessage()).contains("email");
    }

    // ── verifyEmail ───────────────────────────────────────────────────────

    /**
     * Test: verifyEmail() returns 200 OK for a valid token.
     */
    @Test
    @DisplayName("verifyEmail() - valid token returns 200 OK")
    void verifyEmail_validToken_returns200() {
        doNothing().when(authService).verifyEmail("valid-token");

        ResponseEntity<ApiResponse<Void>> response = authController.verifyEmail("valid-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(authService).verifyEmail("valid-token");
    }

    /**
     * Test: verifyEmail() propagates InvalidTokenException for an expired token.
     */
    @Test
    @DisplayName("verifyEmail() - expired token propagates InvalidTokenException")
    void verifyEmail_expiredToken_propagatesInvalidTokenException() {
        doThrow(new InvalidTokenException("Verification token has expired"))
                .when(authService).verifyEmail("expired-token");

        assertThatThrownBy(() -> authController.verifyEmail("expired-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    /**
     * Test: verifyEmail() propagates InvalidTokenException for an already-used token.
     */
    @Test
    @DisplayName("verifyEmail() - already-used token propagates InvalidTokenException")
    void verifyEmail_usedToken_propagatesInvalidTokenException() {
        doThrow(new InvalidTokenException("Verification token has already been used"))
                .when(authService).verifyEmail("used-token");

        assertThatThrownBy(() -> authController.verifyEmail("used-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("already been used");
    }

    // ── login ─────────────────────────────────────────────────────────────

    /**
     * Test: login() returns 200 OK with a login response on valid credentials.
     */
    @Test
    @DisplayName("login() - valid credentials returns 200 OK")
    void login_validCredentials_returns200() {
        AuthDto.LoginResponse loginResponse = AuthDto.LoginResponse.builder()
                .token("jwt-token-xyz").userId(1L)
                .name("Hamza Ali").email("hamza@test.com").role("USER").build();

        when(authService.login(any())).thenReturn(loginResponse);

        ResponseEntity<ApiResponse<AuthDto.LoginResponse>> response =
                authController.login(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    /**
     * Test: login() response body contains the JWT token.
     */
    @Test
    @DisplayName("login() - response body data contains JWT token")
    void login_validCredentials_bodyContainsToken() {
        AuthDto.LoginResponse loginResponse = AuthDto.LoginResponse.builder()
                .token("jwt-token-xyz").userId(1L)
                .name("Hamza Ali").email("hamza@test.com").role("USER").build();

        when(authService.login(any())).thenReturn(loginResponse);

        ResponseEntity<ApiResponse<AuthDto.LoginResponse>> response =
                authController.login(loginRequest);

        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getToken()).isEqualTo("jwt-token-xyz");
        assertThat(response.getBody().getData().getRole()).isEqualTo("USER");
    }

    /**
     * Test: login() propagates BusinessException when email is not verified.
     */
    @Test
    @DisplayName("login() - unverified email propagates BusinessException")
    void login_unverifiedEmail_propagatesBusinessException() {
        doThrow(new BusinessException("Please verify your email before logging in"))
                .when(authService).login(any());

        assertThatThrownBy(() -> authController.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("verify your email");
    }

    /**
     * Test: login() delegates to authService exactly once.
     */
    @Test
    @DisplayName("login() - delegates to authService exactly once")
    void login_delegatesToAuthServiceOnce() {
        when(authService.login(any())).thenReturn(
                AuthDto.LoginResponse.builder().token("t").userId(1L)
                        .name("n").email("e").role("USER").build());

        authController.login(loginRequest);

        verify(authService, times(1)).login(loginRequest);
    }

    // ── forgotPassword ────────────────────────────────────────────────────

    /**
     * Test: forgotPassword() returns 200 OK regardless of whether the email exists.
     * This prevents user enumeration attacks.
     */
    @Test
    @DisplayName("forgotPassword() - always returns 200 to prevent user enumeration")
    void forgotPassword_anyEmail_returns200() {
        doNothing().when(authService).forgotPassword(any());

        ResponseEntity<ApiResponse<Void>> response = authController.forgotPassword(forgotRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    /**
     * Test: forgotPassword() delegates to service exactly once.
     */
    @Test
    @DisplayName("forgotPassword() - delegates to authService exactly once")
    void forgotPassword_delegatesToServiceOnce() {
        doNothing().when(authService).forgotPassword(any());

        authController.forgotPassword(forgotRequest);

        verify(authService, times(1)).forgotPassword(forgotRequest);
        verifyNoMoreInteractions(authService);
    }

    // ── resetPassword ─────────────────────────────────────────────────────

    /**
     * Test: resetPassword() returns 200 OK for a valid token and new password.
     */
    @Test
    @DisplayName("resetPassword() - valid request returns 200 OK")
    void resetPassword_validRequest_returns200() {
        doNothing().when(authService).resetPassword(any());

        ResponseEntity<ApiResponse<Void>> response = authController.resetPassword(resetRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(authService, times(1)).resetPassword(resetRequest);
    }

    /**
     * Test: resetPassword() propagates InvalidTokenException for an unknown token.
     */
    @Test
    @DisplayName("resetPassword() - unknown token propagates InvalidTokenException")
    void resetPassword_unknownToken_propagatesInvalidTokenException() {
        doThrow(new InvalidTokenException("Reset token is invalid"))
                .when(authService).resetPassword(any());

        assertThatThrownBy(() -> authController.resetPassword(resetRequest))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("invalid");
    }

    /**
     * Test: resetPassword() propagates InvalidTokenException for an already-used token.
     */
    @Test
    @DisplayName("resetPassword() - used token propagates InvalidTokenException")
    void resetPassword_usedToken_propagatesInvalidTokenException() {
        doThrow(new InvalidTokenException("Reset token has already been used"))
                .when(authService).resetPassword(any());

        assertThatThrownBy(() -> authController.resetPassword(resetRequest))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("already been used");
    }
}
