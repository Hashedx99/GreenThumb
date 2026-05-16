package com.greenthumb.shared.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility component for generating and validating JWT tokens.
 * <p>
 * Uses the HS256 algorithm with a secret key configured via
 * application properties.
 * </p>
 *
 * @author Hamza Ali
 */
@Component
@Slf4j
public class JwtUtil {

    /** Secret key used to sign JWT tokens. */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Token validity duration in milliseconds. */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates a JWT token for the given user.
     *
     * @param userDetails the authenticated user details
     * @return signed JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Embed the username (email) as the subject
        return buildToken(claims, userDetails.getUsername());
    }

    /**
     * Extracts the username (email) embedded in the token.
     *
     * @param token the JWT token string
     * @return the username (email) from the token subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Checks whether a token is valid for the given user.
     *
     * @param token       the JWT token string
     * @param userDetails the user to validate against
     * @return true if the token is valid and not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extracts a specific claim from the token using a resolver function.
     *
     * @param <T>            the return type of the claim
     * @param token          the JWT token string
     * @param claimsResolver function to extract the desired claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Builds and signs a JWT token with the given claims and subject.
     *
     * @param extraClaims additional claims to embed in the token
     * @param subject     the token subject (email)
     * @return signed JWT token string
     */
    private String buildToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parses and returns all claims from the token.
     *
     * @param token the JWT token string
     * @return Claims object containing all token claims
     * @throws JwtException if the token is invalid or tampered
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns whether the token has passed its expiration date.
     *
     * @param token the JWT token string
     * @return true if the token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Builds the signing key from the configured secret string.
     *
     * @return HMAC-SHA signing key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
