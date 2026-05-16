package com.greenthumb.feature.auth;

import com.greenthumb.feature.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a one-time-use token for email verification
 * or password reset flows.
 *
 * @author Hamza Ali
 */
@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The random UUID token value sent in the email link. */
    @Column(nullable = false, unique = true)
    private String token;

    /** The user this token belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The purpose of this token. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    /** When this token expires — 24 hours for verification, 1 hour for reset. */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Whether the token has already been used. */
    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    /**
     * Checks whether this token has passed its expiration time.
     *
     * @return true if the token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
