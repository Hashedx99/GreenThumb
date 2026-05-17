package com.greenthumb.feature.user;

import com.greenthumb.shared.exception.BusinessException;
import com.greenthumb.shared.exception.ResourceNotFoundException;
import com.greenthumb.shared.response.ApiResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserController} using pure Mockito.
 * Controller methods are called directly — no Spring context loaded.
 *
 * @author Hamza Ali
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDetails userDetails;
    private UserDto.UserResponse userResponse;

    /**
     * Builds reusable fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        // Spring Security UserDetails stub using the builder
        userDetails = User.withUsername("hamza@test.com")
                .password("hashed").roles("USER").build();

        userResponse = UserDto.UserResponse.builder()
                .id(1L).name("Hamza Ali").email("hamza@test.com")
                .role("USER").status("ACTIVE").createdAt(LocalDateTime.now()).build();
    }

    // ── GET /api/users/profile ────────────────────────────────────────────

    /**
     * Test: getMyProfile() returns 200 OK with the user's profile data.
     */
    @Test
    @DisplayName("getMyProfile() - authenticated user returns 200 with profile")
    void getMyProfile_authenticated_returns200WithProfile() {
        when(userService.getMyProfile("hamza@test.com")).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserDto.UserResponse>> response =
                userController.getMyProfile(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getEmail()).isEqualTo("hamza@test.com");
        assertThat(response.getBody().getData().getName()).isEqualTo("Hamza Ali");
    }

    /**
     * Test: getMyProfile() delegates to userService with correct email.
     */
    @Test
    @DisplayName("getMyProfile() - delegates with authenticated user's email")
    void getMyProfile_delegatesWithCorrectEmail() {
        when(userService.getMyProfile("hamza@test.com")).thenReturn(userResponse);

        userController.getMyProfile(userDetails);

        verify(userService, times(1)).getMyProfile("hamza@test.com");
    }

    /**
     * Test: getMyProfile() propagates ResourceNotFoundException for unknown user.
     */
    @Test
    @DisplayName("getMyProfile() - unknown email propagates ResourceNotFoundException")
    void getMyProfile_unknownEmail_propagatesResourceNotFoundException() {
        when(userService.getMyProfile(any()))
                .thenThrow(new ResourceNotFoundException("User not found"));

        assertThatThrownBy(() -> userController.getMyProfile(userDetails))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── PUT /api/users/profile ─────────────────────────────────────────────

    /**
     * Test: updateMyProfile() returns 200 OK with updated user data.
     */
    @Test
    @DisplayName("updateMyProfile() - valid request returns 200 with updated profile")
    void updateMyProfile_validRequest_returns200() {
        UserDto.UpdateProfileRequest request = new UserDto.UpdateProfileRequest();
        request.setName("Hamza Updated");
        request.setBio("Plant lover");

        UserDto.UserResponse updated = UserDto.UserResponse.builder()
                .id(1L).name("Hamza Updated").email("hamza@test.com")
                .role("USER").status("ACTIVE").createdAt(LocalDateTime.now()).build();

        when(userService.updateMyProfile(eq("hamza@test.com"), any())).thenReturn(updated);

        ResponseEntity<ApiResponse<UserDto.UserResponse>> response =
                userController.updateMyProfile(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getName()).isEqualTo("Hamza Updated");
    }

    /**
     * Test: updateMyProfile() delegates to service with email from UserDetails.
     */
    @Test
    @DisplayName("updateMyProfile() - delegates with email from UserDetails")
    void updateMyProfile_delegatesWithUserDetailsEmail() {
        UserDto.UpdateProfileRequest request = new UserDto.UpdateProfileRequest();
        request.setName("Updated Name");
        when(userService.updateMyProfile(eq("hamza@test.com"), any())).thenReturn(userResponse);

        userController.updateMyProfile(userDetails, request);

        verify(userService, times(1)).updateMyProfile(eq("hamza@test.com"), any());
    }

    // ── POST /api/users/change-password ───────────────────────────────────

    /**
     * Test: changePassword() returns 200 OK when current password is correct.
     */
    @Test
    @DisplayName("changePassword() - correct current password returns 200 OK")
    void changePassword_correctCurrentPassword_returns200() {
        UserDto.ChangePasswordRequest request = new UserDto.ChangePasswordRequest();
        request.setCurrentPassword("OldPass@123");
        request.setNewPassword("NewPass@123");

        doNothing().when(userService).changePassword(anyString(), any());

        ResponseEntity<ApiResponse<Void>> response =
                userController.changePassword(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    /**
     * Test: changePassword() propagates BusinessException for wrong current password.
     */
    @Test
    @DisplayName("changePassword() - wrong current password propagates BusinessException")
    void changePassword_wrongCurrentPassword_propagatesBusinessException() {
        UserDto.ChangePasswordRequest request = new UserDto.ChangePasswordRequest();
        request.setCurrentPassword("WrongPass");
        request.setNewPassword("NewPass@123");

        doThrow(new BusinessException("Current password is incorrect"))
                .when(userService).changePassword(anyString(), any());

        assertThatThrownBy(() -> userController.changePassword(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    /**
     * Test: changePassword() delegates to service exactly once.
     */
    @Test
    @DisplayName("changePassword() - delegates to service once")
    void changePassword_delegatesToServiceOnce() {
        UserDto.ChangePasswordRequest request = new UserDto.ChangePasswordRequest();
        request.setCurrentPassword("OldPass@123");
        request.setNewPassword("NewPass@123");
        doNothing().when(userService).changePassword(anyString(), any());

        userController.changePassword(userDetails, request);

        verify(userService, times(1)).changePassword("hamza@test.com", request);
    }

    // ── DELETE /api/admin/users/{id} ──────────────────────────────────────

    /**
     * Test: softDeleteUser() returns 200 OK and success message.
     */
    @Test
    @DisplayName("softDeleteUser() - existing user returns 200 OK")
    void softDeleteUser_existingUser_returns200() {
        doNothing().when(userService).softDeleteUser(2L);

        ResponseEntity<ApiResponse<Void>> response = userController.softDeleteUser(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("deactivated");
    }

    /**
     * Test: softDeleteUser() propagates ResourceNotFoundException for unknown ID.
     */
    @Test
    @DisplayName("softDeleteUser() - unknown id propagates ResourceNotFoundException")
    void softDeleteUser_unknownId_propagatesResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("User", 99L))
                .when(userService).softDeleteUser(99L);

        assertThatThrownBy(() -> userController.softDeleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * Test: softDeleteUser() delegates to service with the correct ID.
     */
    @Test
    @DisplayName("softDeleteUser() - delegates with correct user ID")
    void softDeleteUser_delegatesWithCorrectId() {
        doNothing().when(userService).softDeleteUser(5L);

        userController.softDeleteUser(5L);

        verify(userService, times(1)).softDeleteUser(5L);
    }

    // ── GET /api/admin/users ───────────────────────────────────────────────

    /**
     * Test: getAllUsers() returns 200 OK with a paginated list of user summaries.
     */
    @Test
    @DisplayName("getAllUsers() - returns 200 with paginated user summaries")
    void getAllUsers_returnsPaginatedSummaries() {
        UserDto.UserSummary summary = UserDto.UserSummary.builder()
                .id(1L).name("Hamza Ali").email("hamza@test.com")
                .role("USER").status("ACTIVE").createdAt(LocalDateTime.now()).build();

        Page<UserDto.UserSummary> page = new PageImpl<>(List.of(summary));
        when(userService.getAllUsers(any())).thenReturn(page);

        ResponseEntity<ApiResponse<Page<UserDto.UserSummary>>> response =
                userController.getAllUsers(PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        assertThat(response.getBody().getData().getContent().get(0).getEmail())
                .isEqualTo("hamza@test.com");
    }

    /**
     * Test: getAllUsers() returns 200 with empty page when no users exist.
     */
    @Test
    @DisplayName("getAllUsers() - empty result returns 200 with empty page")
    void getAllUsers_emptyResult_returns200WithEmptyPage() {
        when(userService.getAllUsers(any())).thenReturn(Page.empty());

        ResponseEntity<ApiResponse<Page<UserDto.UserSummary>>> response =
                userController.getAllUsers(PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).isEmpty();
    }
}
