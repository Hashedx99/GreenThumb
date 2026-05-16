package com.greenthumb.feature.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link VerificationToken} persistence operations.
 *
 * @author Hamza Ali
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Finds a token by its string value and type.
     *
     * @param token     the token string to look up
     * @param tokenType the type of token (EMAIL_VERIFICATION or PASSWORD_RESET)
     * @return an Optional containing the token if found
     */
    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);
}
