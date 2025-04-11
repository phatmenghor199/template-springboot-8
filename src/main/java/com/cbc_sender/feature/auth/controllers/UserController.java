package com.cbc_sender.feature.auth.controllers;

import com.cbc_sender.feature.auth.dto.response.UserResponseDto;
import com.cbc_sender.feature.auth.dto.response.AllUserResponseDto;
import com.cbc_sender.exceptions.response.ApiResponse;
import com.cbc_sender.feature.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    @PostMapping()
    public ApiResponse<AllUserResponseDto> getAllUser(
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
    public ApiResponse<UserResponseDto> getUserDetail(@PathVariable Long id) {
        return new ApiResponse<>("success", "User by id response successfully", userService.getUserById(id));
    }

    @PostMapping("/token")
    public ApiResponse<UserResponseDto> getUserByToken() {
        return new ApiResponse<>("success", "User by token response successfully", userService.getUserByToken());
    }

    @PostMapping("delete/{id}")
    public ApiResponse<UserResponseDto> deleteUserById(@PathVariable("id") Long userId) {
        UserResponseDto userDto = userService.deleteUserId(userId);
        return new ApiResponse<>("success", "User delete successfully", userDto);
    }
}
