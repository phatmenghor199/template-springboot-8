package com.mailsender.api.controllers;

import com.mailsender.api.dto.AuthResponseDTO;
import com.mailsender.api.dto.LoginDto;
import com.mailsender.api.dto.RegisterDto;
import com.mailsender.api.dto.UserDto;
import com.mailsender.api.exceptions.ApiResponse;
import com.mailsender.api.exceptions.BadRequestException;
import com.mailsender.api.exceptions.DuplicateNameException;
import com.mailsender.api.exceptions.NotFoundException;
import com.mailsender.api.mapper.UserMapper;
import com.mailsender.api.models.Role;
import com.mailsender.api.models.UserEntity;
import com.mailsender.api.repository.RoleRepository;
import com.mailsender.api.repository.UserRepository;
import com.mailsender.api.request.ChangePasswordRequestDto;
import com.mailsender.api.security.JWTGenerator;
import com.mailsender.api.service.UserService;
import com.mailsender.api.utils.SecurityUtils;
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
    public ApiResponse<AuthResponseDTO> login(@RequestBody LoginDto loginDto) {
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
        UserDto userDto = UserMapper.mapToDto(userEntity);

        return new ApiResponse<>("success",
                "Login successfully",
                new AuthResponseDTO(token, userDto));
    }

    @PostMapping("register")
    public ApiResponse<UserDto> register(@Valid @RequestBody RegisterDto registerDto) {
        // Check if email is already in use
        if (userRepository.existsByUsername(registerDto.getEmail())) {
            throw new DuplicateNameException("Email  is already in use, please choose another one.");
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
