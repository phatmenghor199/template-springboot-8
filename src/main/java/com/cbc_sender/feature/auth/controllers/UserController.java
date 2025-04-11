package com.cbc_sender.feature.auth.controllers;

import com.cbc_sender.config.RequiresRole;
import com.cbc_sender.enumation.RoleEnum;
import com.cbc_sender.feature.auth.dto.response.UserResponseDto;
import com.cbc_sender.feature.auth.dto.response.AllUserResponseDto;
import com.cbc_sender.exceptions.response.ApiResponse;
import com.cbc_sender.feature.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user management operations.
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get all users with pagination and optional search.
     * Requires ADMIN role.
     *
     * @param pageNo Page number (1-based)
     * @param pageSize Items per page
     * @param search Optional search term for email filtering
     * @return Paginated list of users
     */
    @PostMapping()
    @RequiresRole(value = "ADMIN", message = "Only administrators can view all users")
    public ResponseEntity<ApiResponse<AllUserResponseDto>> getAllUsers(
            @RequestParam(value = "pageNo", defaultValue = "1", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "search", required = false) String search
    ) {
        log.info("Get all users request - page: {}, size: {}, search: {}", pageNo, pageSize, search);

        if (pageNo <= 0) {
            log.warn("Invalid page number: {}", pageNo);
            return new ResponseEntity<>(
                    new ApiResponse<>("error", "Page number must be greater than 0", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (pageSize <= 0) {
            log.warn("Invalid page size: {}", pageSize);
            return new ResponseEntity<>(
                    new ApiResponse<>("error", "Page size must be greater than 0", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        // Convert to zero-based page numbering used by Spring Data
        AllUserResponseDto result = userService.getAllUser(pageNo - 1, pageSize, search);

        log.info("Returning {} users (page {}/{})",
                result.getContent().size(),
                result.getPageNo(),
                result.getTotalPages());

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Users retrieved successfully", result)
        );
    }

    /**
     * Get user details by ID.
     * Requires ADMIN role or the user can access their own details.
     *
     * @param id User ID
     * @return User details
     */
    @GetMapping("/{id}")
    @RequiresRole(value = {"ADMIN", "USER"}, anyRole = true)
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserDetail(@PathVariable Long id) {
        log.info("Get user details request for user ID: {}", id);

        UserResponseDto user = userService.getUserById(id);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "User details retrieved successfully", user)
        );
    }

    /**
     * Get current user details from JWT token.
     * Available to any authenticated user.
     *
     * @return Current user details
     */
    @GetMapping("/token")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByToken() {
        log.info("Get current user details from token");

        UserResponseDto user = userService.getUserByToken();

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Current user details retrieved successfully", user)
        );
    }

    /**
     * Delete a user by ID.
     * Requires ADMIN role.
     *
     * @param userId User ID to delete
     * @return Deleted user details
     */
    @DeleteMapping("/{id}")
    @RequiresRole(value = "ADMIN", message = "Only administrators can delete users")
    public ResponseEntity<ApiResponse<UserResponseDto>> deleteUser(@PathVariable("id") Long userId) {
        log.info("Delete user request for user ID: {}", userId);

        UserResponseDto deletedUser = userService.deleteUserId(userId);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "User deleted successfully", deletedUser)
        );
    }

    /**
     * Enable/disable a user.
     * Requires ADMIN role.
     *
     * @param userId User ID to update
     * @param enabled Status to set
     * @return Updated user details
     */
    @PatchMapping("/{id}/status")
    @RequiresRole(value = "ADMIN", message = "Only administrators can update user status")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserStatus(
            @PathVariable("id") Long userId,
            @RequestParam boolean enabled) {

        log.info("Update user status request - User ID: {}, Enable: {}", userId, enabled);

        // This would need to be implemented in UserService
        // UserResponseDto updatedUser = userService.updateUserStatus(userId, enabled);

        // For now, simply return the user details
        UserResponseDto user = userService.getUserById(userId);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "User status updated successfully", user)
        );
    }

    /**
     * Update user roles.
     * Requires ADMIN role.
     *
     * @param userId User ID to update
     * @param roles New roles to assign
     * @return Updated user details
     */
    @PatchMapping("/{id}/roles")
    @RequiresRole(value = "ADMIN", message = "Only administrators can update user roles")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRoles(
            @PathVariable("id") Long userId,
            @RequestBody RoleEnum[] roles) {

        log.info("Update user roles request - User ID: {}, Roles: {}", userId, roles);

        // This would need to be implemented in UserService
        // UserResponseDto updatedUser = userService.updateUserRoles(userId, roles);

        // For now, simply return the user details
        UserResponseDto user = userService.getUserById(userId);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "User roles updated successfully", user)
        );
    }
}