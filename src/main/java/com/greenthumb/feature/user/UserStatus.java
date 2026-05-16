package com.greenthumb.feature.user;

/**
 * Represents the lifecycle status of a user account.
 *
 * @author Hamza Ali
 */
public enum UserStatus {

    /** Account created but email not yet verified. */
    PENDING,

    /** Account fully active — email verified. */
    ACTIVE,

    /** Account soft-deleted by an admin — cannot log in. */
    INACTIVE
}
