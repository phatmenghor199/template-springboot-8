package com.cbc_sender.config;

import com.cbc_sender.enumation.RoleEnum;
import com.cbc_sender.feature.auth.models.Role;
import com.cbc_sender.feature.auth.models.UserEntity;
import com.cbc_sender.feature.auth.repository.RoleRepository;
import com.cbc_sender.feature.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Initializes default roles and admin user on application startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        log.info("Initializing default roles and admin user...");

        // Create each role if it doesn't already exist
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            if (!roleRepository.existsByName(roleEnum)) {
                Role role = new Role();
                role.setName(roleEnum);
                roleRepository.save(role);
                log.info("Created role: {}", roleEnum);
            } else {
                log.debug("Role already exists: {}", roleEnum);
            }
        });

        // Create default admin user if not exists
        createAdminUser();

        log.info("Role and admin initialization completed");
    }

    /**
     * Creates a default admin user if no admin exists.
     */
    private void createAdminUser() {
        // Check if admin user already exists
        Optional<Role> adminRole = roleRepository.findByName(RoleEnum.ADMIN);

        if (!adminRole.isPresent()) {
            log.error("Admin role not found, cannot create admin user");
            return;
        }

        // Check if we already have at least one admin
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleEnum.ADMIN));

        if (adminExists) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        // Create new admin user
        UserEntity adminUser = new UserEntity();
        adminUser.setUsername(adminEmail);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setRoles(Collections.singletonList(adminRole.get()));

        userRepository.save(adminUser);
        log.info("Created default admin user: {}", adminEmail);
    }
}