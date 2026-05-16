package com.greenthumb.shared.exception;

/**
 * Thrown when a request violates a business rule
 * (e.g. logging care for a plant you don't own).
 *
 * @author Hamza Ali
 */
public class BusinessException extends RuntimeException {

    /**
     * Constructs a new BusinessException with the given message.
     *
     * @param message description of the violated business rule
     */
    public BusinessException(String message) {
        super(message);
    }
}
