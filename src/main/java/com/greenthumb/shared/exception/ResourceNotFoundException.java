package com.greenthumb.shared.exception;

/**
 * Thrown when a requested resource cannot be found.
 *
 * @author Hamza Ali
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the given message.
     *
     * @param message description of the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience constructor for entity lookups by ID.
     *
     * @param entityName name of the entity type
     * @param id         the ID that was not found
     */
    public ResourceNotFoundException(String entityName, Long id) {
        super(entityName + " not found with id: " + id);
    }
}
