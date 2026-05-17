package com.greenthumb.feature.auth;

import com.greenthumb.feature.user.*;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.DuplicateResourceException;
import com.greenthumb.shared.exception.InvalidTokenException;
import com.greenthumb.shared.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 * All dependencies are mocked — no Spring context loaded.
 *
 * @author Hamza Ali
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private AuthDto.RegisterRequest registerRequest;

    /**
     * Sets up reusable test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .name("Hamza Ali")
                .email("hamza@test.com")
                .password("hashed-password")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        registerRequest = new AuthDto.RegisterRequest();
        registerRequest.setName("Hamza Ali");
        registerRequest.setEmail("hamza@test.com");
        registerRequest.setPassword("SecurePass1!");
    }

    // ── register ─────────────────────────────────────────────────────────

    /**
     * Test: register() throws DuplicateResourceException when email already exists.
     */
    @Test
    @DisplayName("register() - duplicate email throws DuplicateResourceException")
    void register_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("hamza@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");
    }

    /**
     * Test: register() saves user and sends email on valid request.
     */
    @Test
    @DisplayName("register() - valid request saves user and sends verification email")
    void register_validRequest_savesUserAndSendsEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);
        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(
                VerificationToken.builder().token("token-123").build());
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>email</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        authService.register(registerRequest);

        // Verify user was saved and email was dispatched
        verify(userRepository, times(1)).save(any(User.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ── verifyEmail ───────────────────────────────────────────────────────

    /**
     * Test: verifyEmail() throws InvalidTokenException for unknown token.
     */
    @Test
    @DisplayName("verifyEmail() - unknown token throws InvalidTokenException")
    void verifyEmail_unknownToken_throwsInvalidTokenException() {
        when(tokenRepository.findByTokenAndTokenType(anyString(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("bad-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    /**
     * Test: verifyEmail() throws InvalidTokenException for expired token.
     */
    @Test
    @DisplayName("verifyEmail() - expired token throws InvalidTokenException")
    void verifyEmail_expiredToken_throwsInvalidTokenException() {
        VerificationToken expiredToken = VerificationToken.builder()
                .token("expired-token")
                .user(activeUser)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(tokenRepository.findByTokenAndTokenType(anyString(), any()))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.verifyEmail("expired-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    /**
     * Test: verifyEmail() activates user account on valid token.
     */
    @Test
    @DisplayName("verifyEmail() - valid token activates user account")
    void verifyEmail_validToken_activatesUser() {
        User pendingUser = User.builder()
                .id(2L)
                .email("new@test.com")
                .status(UserStatus.PENDING)
                .emailVerified(false)
                .build();

        VerificationToken validToken = VerificationToken.builder()
                .token("valid-token")
                .user(pendingUser)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(23))
                .used(false)
                .build();

        when(tokenRepository.findByTokenAndTokenType(anyString(), any()))
                .thenReturn(Optional.of(validToken));
        when(userRepository.save(any(User.class))).thenReturn(pendingUser);
        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(validToken);

        authService.verifyEmail("valid-token");

        // Verify user status was updated to ACTIVE
        assertThat(pendingUser.isEmailVerified()).isTrue();
        assertThat(pendingUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    // ── login ─────────────────────────────────────────────────────────────

    /**
     * Test: login() returns JWT token for verified user.
     */
    @Test
    @DisplayName("login() - verified user returns JWT in response")
    void login_verifiedUser_returnsJwt() {
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setEmail("hamza@test.com");
        loginRequest.setPassword("SecurePass1!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("hamza@test.com"))
                .thenReturn(Optional.of(activeUser));
        when(jwtUtil.generateToken(any())).thenReturn("jwt-token-abc");

        AuthDto.LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token-abc");
        assertThat(response.getEmail()).isEqualTo("hamza@test.com");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    /**
     * Test: login() throws BusinessException if email not verified.
     */
    @Test
    @DisplayName("login() - unverified email throws BusinessException")
    void login_unverifiedEmail_throwsBusinessException() {
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setEmail("hamza@test.com");
        loginRequest.setPassword("SecurePass1!");

        User unverifiedUser = User.builder()
                .id(3L)
                .email("hamza@test.com")
                .emailVerified(false)
                .status(UserStatus.PENDING)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("hamza@test.com"))
                .thenReturn(Optional.of(unverifiedUser));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("verify your email");
    }

    // ── resetPassword ─────────────────────────────────────────────────────

    /**
     * Test: resetPassword() throws InvalidTokenException for used token.
     */
    @Test
    @DisplayName("resetPassword() - used token throws InvalidTokenException")
    void resetPassword_usedToken_throwsInvalidTokenException() {
        VerificationToken usedToken = VerificationToken.builder()
                .token("used-token")
                .user(activeUser)
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        when(tokenRepository.findByTokenAndTokenType(anyString(), any()))
                .thenReturn(Optional.of(usedToken));

        AuthDto.ResetPasswordRequest request = new AuthDto.ResetPasswordRequest();
        request.setToken("used-token");
        request.setNewPassword("NewPass@123");

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("already been used");
    }
}
