package com.greenthumb.feature.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link User} entity persistence operations.
 *
 * @author Hamza Ali
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check
     * @return true if a user with that email exists
     */
    boolean existsByEmail(String email);
}
