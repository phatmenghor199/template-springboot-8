package com.bkgoutchecker.api.controllers;

import com.bkgoutchecker.api.dto.*;
import com.bkgoutchecker.api.exceptions.ApiResponse;
import com.bkgoutchecker.api.exceptions.BadRequestException;
import com.bkgoutchecker.api.mapper.UserMapper;
import com.bkgoutchecker.api.models.Role;
import com.bkgoutchecker.api.models.UserEntity;
import com.bkgoutchecker.api.repository.RoleRepository;
import com.bkgoutchecker.api.repository.UserRepository;
import com.bkgoutchecker.api.security.JWTGenerator;
import com.bkgoutchecker.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;


    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          RoleRepository roleRepository, PasswordEncoder passwordEncoder, JWTGenerator jwtGenerator, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("login")
    public ApiResponse<AuthResponseDTO> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        return new ApiResponse<>("success",
                "Login successfully",
                new AuthResponseDTO(token));
    }

    @PostMapping("register")
    public ApiResponse<UserDto> register(@RequestBody RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new BadRequestException("failed", "Username is already in use, please choose another one.", 400);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode((registerDto.getPassword())));
        Role roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));
        final UserEntity save = userRepository.save(user);
        return new ApiResponse<>("success", "Your register successfully", UserMapper.mapToDto(save));
    }

}
