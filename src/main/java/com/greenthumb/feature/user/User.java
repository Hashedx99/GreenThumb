package com.greenthumb.feature.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entity representing a GreenThumb user.
 * <p>
 * Implements {@link UserDetails} so Spring Security can use it directly
 * for authentication and authorization checks.
 * </p>
 *
 * @author Hamza Ali
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    /** Primary key — auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User's full display name. */
    @Column(nullable = false)
    private String name;

    /** Unique email used for login and communication. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password — never stored in plain text. */
    @Column(nullable = false)
    private String password;

    /** Role determining access level (USER or ADMIN). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    /** Account status — soft-delete sets this to INACTIVE. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    /** Whether the user has clicked the email verification link. */
    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    /** Cloudinary URL for the user's profile picture. */
    private String profilePictureUrl;

    /** Short bio displayed on the user's profile. */
    private String bio;

    /** Optional location field (e.g. city). */
    private String location;

    /** Timestamp of account creation. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── UserDetails interface ──────────────────────────────────────────────

    /**
     * Returns the user's authorities derived from their role.
     *
     * @return a single authority matching the user's role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the username (email) used by Spring Security.
     *
     * @return the user's email address
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the account is non-expired.
     *
     * @return true always — expiry not implemented
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is non-locked.
     * Inactive (soft-deleted) users are considered locked.
     *
     * @return true if the account is ACTIVE or PENDING
     */
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.INACTIVE;
    }

    /**
     * Indicates whether credentials are non-expired.
     *
     * @return true always — credential expiry not implemented
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is enabled.
     * Only ACTIVE users (email verified) can log in.
     *
     * @return true if the user's email has been verified
     */
    @Override
    public boolean isEnabled() {
        return emailVerified && status == UserStatus.ACTIVE;
    }
}
