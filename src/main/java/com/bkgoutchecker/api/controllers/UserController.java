package com.bkgoutchecker.api.controllers;

import com.bkgoutchecker.api.dto.UserDto;
import com.bkgoutchecker.api.dto.UserResponseDto;
import com.bkgoutchecker.api.exceptions.ApiResponse;
import com.bkgoutchecker.api.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping()
    public ApiResponse<UserResponseDto> getAllUser(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ) {
        return new ApiResponse<>("success", "User response successfully", userService.getAllUser(pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> pokemonDetail(@PathVariable Long id) {
        return new ApiResponse<>("success", "User response successfully", userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<UserDto> deletePokemon(@PathVariable("id") Long userId) {
        UserDto userDto = userService.deleteUserId(userId);
        return new ApiResponse<>("success", "User delete successfully", userDto);
    }
}
