package com.greenthumb.feature.user;

import com.cloudinary.Cloudinary;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Implementation of {@link UserService} handling user profile
 * management and admin user operations.
 *
 * @author Hamza Ali
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDto.UserResponse getMyProfile(String email) {
        User user = findUserByEmail(email);
        return UserDto.UserResponse.from(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserDto.UserResponse updateMyProfile(String email, UserDto.UpdateProfileRequest request) {
        User user = findUserByEmail(email);

        // Apply only the fields provided in the request
        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setLocation(request.getLocation());

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", email);
        return UserDto.UserResponse.from(saved);
    }

    /**
     * {@inheritDoc}
     * Uploads the image to Cloudinary under the "greenthumb/profiles" folder.
     */
    @Override
    @Transactional
    public UserDto.UserResponse uploadProfilePicture(String email, MultipartFile file) {
        User user = findUserByEmail(email);

        try {
            // Upload image to Cloudinary and retrieve the secure URL
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "greenthumb/profiles")
            );
            String imageUrl = (String) result.get("secure_url");
            user.setProfilePictureUrl(imageUrl);
            User saved = userRepository.save(user);
            log.info("Profile picture updated for user: {}", email);
            return UserDto.UserResponse.from(saved);
        } catch (IOException e) {
            log.error("Failed to upload profile picture for user: {}", email, e);
            throw new BusinessException("Failed to upload profile picture. Please try again.");
        }
    }

    /**
     * {@inheritDoc}
     * Verifies the current password before encoding and saving the new one.
     */
    @Override
    @Transactional
    public void changePassword(String email, UserDto.ChangePasswordRequest request) {
        User user = findUserByEmail(email);

        // Verify current password matches before allowing change
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<UserDto.UserSummary> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDto.UserSummary::from);
    }

    /**
     * {@inheritDoc}
     * Sets the user's status to INACTIVE rather than deleting the record.
     */
    @Override
    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User soft-deleted: id={}", userId);
    }

    /**
     * Convenience method to look up a user by email or throw a 404.
     *
     * @param email the user's email
     * @return the found User entity
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
