package com.cbc_sender.feature.auth.controllers;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

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


    @PostMapping("login")
    public ApiResponse<AuthResponseDTO> login(@RequestBody LoginRequestDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        Optional<UserEntity> userEntityOpt = userRepository.findByUsername(loginDto.getEmail());
        if (!userEntityOpt.isPresent()) {
            throw new NotFoundException("User not found");
        }
        UserEntity userEntity = userEntityOpt.get();
        UserResponseDto userDto = UserMapper.mapToDto(userEntity);

        return new ApiResponse<>("success",
                "Login successfully",
                new AuthResponseDTO(token, userDto));
    }

    @PostMapping("register")
    public ApiResponse<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto registerDto) {
        // Check if email is already in use
        if (userRepository.existsByUsername(registerDto.getEmail())) {
            throw new DuplicateNameException("Email is already in use, please choose another one.");
        }

        // Fetch the role safely
        Role role = roleRepository.findByName(registerDto.getRole())
                .orElseThrow(() -> new BadRequestException("Invalid role provided."));

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRoles(Collections.singletonList(role));

        // Save the user
        final UserEntity savedUser = userRepository.save(user);

        // Return success response
        return new ApiResponse<>("success", "You have registered successfully.", UserMapper.mapToDto(savedUser));
    }

}
