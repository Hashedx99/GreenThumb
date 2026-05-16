package com.greenthumb.shared.exception;

/**
 * Thrown when an email verification or password reset token is
 * invalid, expired, or already used.
 *
 * @author Hamza Ali
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Constructs a new InvalidTokenException with the given message.
     *
     * @param message description of the token problem
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}
