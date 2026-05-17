package com.greenthumb.feature.auth;

import com.greenthumb.feature.user.*;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.DuplicateResourceException;
import com.greenthumb.shared.exception.InvalidTokenException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import com.greenthumb.shared.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of {@link AuthService} handling registration,
 * email verification, login, and password reset flows.
 *
 * @author Hamza Ali
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * {@inheritDoc}
     * Throws {@link DuplicateResourceException} if email is already registered.
     */
    @Override
    @Transactional
    public void register(AuthDto.RegisterRequest request) {
        // Reject duplicate email registrations
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }

        // Build new user with PENDING status
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .status(UserStatus.PENDING)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Generate and send verification email
        String token = generateAndSaveToken(user, TokenType.EMAIL_VERIFICATION, 24);
        sendVerificationEmail(user, token);
        log.info("User registered, verification email sent: {}", user.getEmail());
    }

    /**
     * {@inheritDoc}
     * Throws {@link InvalidTokenException} if token is unknown, expired, or already used.
     */
    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository
                .findByTokenAndTokenType(token, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new InvalidTokenException("Verification token is invalid"));

        if (verificationToken.isExpired()) {
            throw new InvalidTokenException("Verification token has expired. Please request a new one.");
        }
        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("Verification token has already been used.");
        }

        // Activate user account
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Mark token as consumed
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        log.info("Email verified for user: {}", user.getEmail());
    }

    /**
     * {@inheritDoc}
     * Delegates authentication to Spring Security's AuthenticationManager.
     */
    @Override
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        // Spring Security handles credential validation and throws on failure
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce email verification before allowing login
        if (!user.isEmailVerified()) {
            throw new BusinessException("Please verify your email before logging in.");
        }

        String jwt = jwtUtil.generateToken(user);
        log.info("User logged in: {}", user.getEmail());

        return AuthDto.LoginResponse.builder()
                .token(jwt)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * {@inheritDoc}
     * Silently succeeds even if email is not found (prevents user enumeration).
     */
    @Override
    @Transactional
    public void forgotPassword(AuthDto.ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = generateAndSaveToken(user, TokenType.PASSWORD_RESET, 1);
            sendPasswordResetEmail(user, token);
            log.info("Password reset email sent to: {}", user.getEmail());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void resetPassword(AuthDto.ResetPasswordRequest request) {
        VerificationToken resetToken = tokenRepository
                .findByTokenAndTokenType(request.getToken(), TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidTokenException("Reset token is invalid"));

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Reset token has expired. Please request a new one.");
        }
        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Reset token has already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset completed for user: {}", user.getEmail());
    }

    /**
     * Generates a random UUID token, saves it to the database,
     * and returns the token string.
     *
     * @param user      the user this token belongs to
     * @param type      the token type (EMAIL_VERIFICATION or PASSWORD_RESET)
     * @param hoursValid number of hours until the token expires
     * @return the generated token string
     */
    private String generateAndSaveToken(User user, TokenType type, int hoursValid) {
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenValue)
                .user(user)
                .tokenType(type)
                .expiresAt(LocalDateTime.now().plusHours(hoursValid))
                .build();
        tokenRepository.save(token);
        return tokenValue;
    }

    /**
     * Sends an email verification link to the user's email address.
     *
     * @param user  the user to send the email to
     * @param token the verification token to include in the link
     */
    private void sendVerificationEmail(User user, String token) {
        String link = baseUrl + "/auth/verify?token=" + token;
        Context ctx = new Context();
        ctx.setVariable("name", user.getName());
        ctx.setVariable("link", link);
        String html = templateEngine.process("email/verify-email", ctx);
        sendEmail(user.getEmail(), "Verify your GreenThumb account", html);
    }

    /**
     * Sends a password reset link to the user's email address.
     *
     * @param user  the user to send the email to
     * @param token the reset token to include in the link
     */
    private void sendPasswordResetEmail(User user, String token) {
        String link = baseUrl + "/auth/reset-password?token=" + token;
        Context ctx = new Context();
        ctx.setVariable("name", user.getName());
        ctx.setVariable("link", link);
        String html = templateEngine.process("email/reset-password", ctx);
        sendEmail(user.getEmail(), "Reset your GreenThumb password", html);
    }

    /**
     * Sends an HTML email using JavaMailSender.
     *
     * @param to      the recipient email address
     * @param subject the email subject line
     * @param html    the HTML body content
     */
    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new BusinessException("Failed to send email. Please try again.");
        }
    }
}
