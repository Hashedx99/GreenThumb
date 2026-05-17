package com.greenthumb.feature.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 *
 * @author Hamza Ali
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    /**
     * Initialises test user fixture before each test.
     */
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Hamza Ali")
                .email("hamza@test.com")
                .password("hashed-password")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }

    // ── getMyProfile ──────────────────────────────────────────────────────

    /**
     * Test: getMyProfile() returns UserResponse for known email.
     */
    @Test
    @DisplayName("getMyProfile() - known email returns UserResponse")
    void getMyProfile_knownEmail_returnsUserResponse() {
        when(userRepository.findByEmail("hamza@test.com")).thenReturn(Optional.of(testUser));

        UserDto.UserResponse result = userService.getMyProfile("hamza@test.com");

        assertThat(result.getEmail()).isEqualTo("hamza@test.com");
        assertThat(result.getName()).isEqualTo("Hamza Ali");
    }

    /**
     * Test: getMyProfile() throws ResourceNotFoundException for unknown email.
     */
    @Test
    @DisplayName("getMyProfile() - unknown email throws ResourceNotFoundException")
    void getMyProfile_unknownEmail_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile("unknown@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateMyProfile ────────────────────────────────────────────────────

    /**
     * Test: updateMyProfile() updates and returns updated UserResponse.
     */
    @Test
    @DisplayName("updateMyProfile() - valid request updates and returns UserResponse")
    void updateMyProfile_validRequest_returnsUpdatedResponse() {
        UserDto.UpdateProfileRequest request = new UserDto.UpdateProfileRequest();
        request.setName("Hamza Updated");
        request.setBio("Plant lover");
        request.setLocation("Bahrain");

        when(userRepository.findByEmail("hamza@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto.UserResponse result = userService.updateMyProfile("hamza@test.com", request);

        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getName()).isEqualTo("Hamza Updated");
        assertThat(testUser.getBio()).isEqualTo("Plant lover");
    }

    // ── changePassword ─────────────────────────────────────────────────────

    /**
     * Test: changePassword() throws BusinessException when current password is wrong.
     */
    @Test
    @DisplayName("changePassword() - wrong current password throws BusinessException")
    void changePassword_wrongCurrentPassword_throwsBusinessException() {
        UserDto.ChangePasswordRequest request = new UserDto.ChangePasswordRequest();
        request.setCurrentPassword("WrongPass");
        request.setNewPassword("NewPass@123");

        when(userRepository.findByEmail("hamza@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPass", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("hamza@test.com", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    /**
     * Test: changePassword() saves new password when current password is correct.
     */
    @Test
    @DisplayName("changePassword() - correct current password saves new password")
    void changePassword_correctCurrentPassword_savesNewPassword() {
        UserDto.ChangePasswordRequest request = new UserDto.ChangePasswordRequest();
        request.setCurrentPassword("CorrectPass");
        request.setNewPassword("NewPass@123");

        when(userRepository.findByEmail("hamza@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("CorrectPass", "hashed-password")).thenReturn(true);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("new-hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword("hamza@test.com", request);

        verify(passwordEncoder).encode("NewPass@123");
        verify(userRepository).save(testUser);
    }

    // ── softDeleteUser ─────────────────────────────────────────────────────

    /**
     * Test: softDeleteUser() sets user status to INACTIVE.
     */
    @Test
    @DisplayName("softDeleteUser() - sets user status to INACTIVE")
    void softDeleteUser_existingUser_setsStatusInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.softDeleteUser(1L);

        assertThat(testUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(testUser);
    }

    /**
     * Test: softDeleteUser() throws ResourceNotFoundException for unknown ID.
     */
    @Test
    @DisplayName("softDeleteUser() - unknown id throws ResourceNotFoundException")
    void softDeleteUser_unknownId_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.softDeleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getAllUsers ────────────────────────────────────────────────────────

    /**
     * Test: getAllUsers() returns paginated UserSummary results.
     */
    @Test
    @DisplayName("getAllUsers() - returns paginated UserSummary list")
    void getAllUsers_returnsPaginatedSummaries() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        Page<UserDto.UserSummary> result = userService.getAllUsers(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("hamza@test.com");
    }
}
