package com.greenthumb.feature.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface defining user profile and admin operations.
 *
 * @author Hamza Ali
 */
public interface UserService {

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param email the authenticated user's email
     * @return the user's profile response DTO
     */
    UserDto.UserResponse getMyProfile(String email);

    /**
     * Updates the profile details of the currently authenticated user.
     *
     * @param email   the authenticated user's email
     * @param request the profile update request body
     * @return the updated user response DTO
     */
    UserDto.UserResponse updateMyProfile(String email, UserDto.UpdateProfileRequest request);

    /**
     * Uploads and sets a new profile picture for the authenticated user.
     *
     * @param email the authenticated user's email
     * @param file  the image file to upload
     * @return the updated user response DTO with new picture URL
     */
    UserDto.UserResponse uploadProfilePicture(String email, MultipartFile file);

    /**
     * Changes the authenticated user's password after verifying the current one.
     *
     * @param email   the authenticated user's email
     * @param request the change password request containing current and new passwords
     */
    void changePassword(String email, UserDto.ChangePasswordRequest request);

    /**
     * Returns a paginated list of all users (admin only).
     *
     * @param pageable pagination parameters
     * @return page of user summaries
     */
    Page<UserDto.UserSummary> getAllUsers(Pageable pageable);

    /**
     * Soft-deletes a user by setting their status to INACTIVE (admin only).
     *
     * @param userId the ID of the user to deactivate
     */
    void softDeleteUser(Long userId);
}
