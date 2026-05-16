package com.greenthumb.shared.exception;

/**
 * Thrown when attempting to create a resource that already exists
 * (e.g. email already registered).
 *
 * @author Hamza Ali
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs a new DuplicateResourceException with the given message.
     *
     * @param message description of the duplicate resource
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
