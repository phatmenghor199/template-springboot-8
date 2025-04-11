package com.cbc_sender.feature.auth.controllers;

import com.cbc_sender.config.RequiresRole;
import com.cbc_sender.enumation.RoleEnum;
import com.cbc_sender.feature.auth.dto.response.AuthResponseDTO;
import com.cbc_sender.feature.auth.dto.request.LoginRequestDto;
import com.cbc_sender.feature.auth.dto.request.RegisterRequestDto;
import com.cbc_sender.feature.auth.dto.response.UserResponseDto;
import com.cbc_sender.feature.auth.mapper.UserMapper;
import com.cbc_sender.feature.auth.models.Role;
import com.cbc_sender.feature.auth.models.UserEntity;
import com.cbc_sender.feature.auth.repository.RoleRepository;
import com.cbc_sender.feature.auth.repository.UserRepository;
import com.cbc_sender.feature.auth.security.JWTGenerator;
import com.cbc_sender.exceptions.error.BadRequestException;
import com.cbc_sender.exceptions.error.DuplicateNameException;
import com.cbc_sender.exceptions.error.NotFoundException;
import com.cbc_sender.exceptions.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

/**
 * Controller for authentication operations like login and registration.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;

    /**
     * Handles user login requests.
     * @param loginDto Login credentials
     * @return Authentication response with JWT token
     */
    @PostMapping("login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDto loginDto) {
        log.info("Login attempt for user: {}", loginDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        Optional<UserEntity> userEntityOpt = userRepository.findByUsername(loginDto.getEmail());
        if (!userEntityOpt.isPresent()) {
            log.warn("User not found with email: {}", loginDto.getEmail());
            throw new NotFoundException("User not found");
        }

        UserEntity userEntity = userEntityOpt.get();
        UserResponseDto userDto = UserMapper.mapToDto(userEntity);

        log.info("User {} logged in successfully", loginDto.getEmail());

        ApiResponse<AuthResponseDTO> response = new ApiResponse<>(
                "success",
                "Login successful",
                new AuthResponseDTO(token, userDto)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Handles user registration.
     * @param registerDto Registration data
     * @return Newly created user information
     */
    @PostMapping("register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequestDto registerDto) {
        log.info("Registration attempt with email: {}", registerDto.getEmail());

        // Check if email is already in use
        if (userRepository.existsByUsername(registerDto.getEmail())) {
            log.warn("Registration failed: Email already in use: {}", registerDto.getEmail());
            throw new DuplicateNameException("Email is already in use, please choose another one.");
        }

        // Validate role
        if (registerDto.getRole() == null) {
            log.warn("Registration failed: No role provided");
            throw new BadRequestException("Role is required for registration.");
        }

        // Ensure role exists in the database
        Role role = roleRepository.findByName(registerDto.getRole())
                .orElseThrow(() -> {
                    log.warn("Registration failed: Invalid role provided: {}", registerDto.getRole());
                    return new BadRequestException("Invalid role provided: " + registerDto.getRole());
                });

        // Create user
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRoles(Collections.singletonList(role));

        // Save the user
        final UserEntity savedUser = userRepository.save(user);
        log.info("User registered successfully with email: {}", registerDto.getEmail());

        // Return success response
        ApiResponse<UserResponseDto> response = new ApiResponse<>(
                "success",
                "Registration successful",
                UserMapper.mapToDto(savedUser)
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all available roles in the system.
     * This endpoint is public for registration purposes.
     * @return List of available roles
     */
    @GetMapping("roles")
    public ResponseEntity<ApiResponse<RoleEnum[]>> getAvailableRoles() {
        log.info("Fetching available roles");

        ApiResponse<RoleEnum[]> response = new ApiResponse<>(
                "success",
                "Available roles retrieved successfully",
                RoleEnum.values()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Check if a token is valid.
     * This is useful for client-side validation without accessing protected resources.
     * @return Simple validation response
     */
    @PostMapping("validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken() {
        // The JWT filter will have already validated the token
        // If we've reached this point, the token is valid

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Token validation request from user: {}", auth.getName());

        ApiResponse<Boolean> response = new ApiResponse<>(
                "success",
                "Token is valid",
                true
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Admin-only endpoint to create a new user with specified role.
     * @param registerDto User data
     * @return Created user information
     */
    @PostMapping("admin/create-user")
    @RequiresRole(value = "ADMIN", message = "Only administrators can create users")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody RegisterRequestDto registerDto) {
        log.info("Admin creating new user with email: {}", registerDto.getEmail());

        // Reuse registration logic
        ResponseEntity<ApiResponse<UserResponseDto>> result = register(registerDto);

        ApiResponse<UserResponseDto> response = result.getBody();
        if (response != null) {
            response.setMessage("User created successfully by administrator");
        }

        return result;
    }
}