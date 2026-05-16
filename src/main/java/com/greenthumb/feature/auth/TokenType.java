package com.greenthumb.feature.auth;

/**
 * Defines the purpose of a {@link VerificationToken}.
 *
 * @author Hamza Ali
 */
public enum TokenType {

    /** Token sent to verify a user's email address on registration. */
    EMAIL_VERIFICATION,

    /** Token sent to allow a user to reset a forgotten password. */
    PASSWORD_RESET
}
