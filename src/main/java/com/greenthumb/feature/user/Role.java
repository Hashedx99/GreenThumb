package com.greenthumb.feature.user;

/**
 * Defines the roles available to GreenThumb users.
 * <p>
 * Used by Spring Security via {@code ROLE_} prefix convention
 * in {@code @PreAuthorize} and security configuration.
 * </p>
 *
 * @author Hamza Ali
 */
public enum Role {

    /** Standard user — can manage their own plants and care data. */
    USER,

    /** Administrator — can manage species catalogue and all users. */
    ADMIN
}
