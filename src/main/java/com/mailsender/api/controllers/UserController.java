package com.mailsender.api.controllers;

import com.mailsender.api.dto.UserDto;
import com.mailsender.api.dto.UserResponseDto;
import com.mailsender.api.exceptions.ApiResponse;
import com.mailsender.api.request.ChangePasswordByAdminRequestDto;
import com.mailsender.api.request.ChangePasswordRequestDto;
import com.mailsender.api.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    @PostMapping()
    public ApiResponse<UserResponseDto> getAllUser(
            @RequestParam(value = "pageNo", defaultValue = "1", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "search", required = false) String search
    ) {
        if (pageNo <= 0) {
            // If pageNo is invalid (e.g., 0 or negative), return an error response
            return new ApiResponse<>("error", "Page number must be greater than 0", null);
        }

        if (pageSize <= 0) {
            // If pageSize is invalid (e.g., 0 or negative), return an error response
            return new ApiResponse<>("error", "Page size must be greater than 0", null);
        }

        return new ApiResponse<>("success", "All User response successfully", userService.getAllUser(pageNo - 1, pageSize, search));
    }

    @PostMapping("/{id}")
    public ApiResponse<UserDto> getUserDetail(@PathVariable Long id) {
        return new ApiResponse<>("success", "User by id response successfully", userService.getUserById(id));
    }

    @PostMapping("/token")
    public ApiResponse<UserDto> getUserByToken() {
        return new ApiResponse<>("success", "User by token response successfully", userService.getUserByToken());
    }

    @PostMapping("delete/{id}")
    public ApiResponse<UserDto> deleteUserById(@PathVariable("id") Long userId) {
        UserDto userDto = userService.deleteUserId(userId);
        return new ApiResponse<>("success", "User delete successfully", userDto);
    }

    @PostMapping("change-password")
    public ApiResponse<UserDto> changePassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordDto) {
        UserDto userDto = userService.changePassword(changePasswordDto);
        return new ApiResponse<>("success", "Password changed successfully.", userDto);
    }

    @PostMapping("change-password-by-admin")
    public ApiResponse<UserDto> changePasswordByAdmin(@Valid @RequestBody ChangePasswordByAdminRequestDto changePasswordDto) {
        UserDto userDto = userService.changePasswordByAdmin(changePasswordDto);
        return new ApiResponse<>("success", "Password changed by admin successfully.", userDto);
    }
}
