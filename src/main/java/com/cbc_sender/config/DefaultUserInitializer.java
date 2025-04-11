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
 * Initializes default roles and users on application startup.
 * This runs when the application starts and ensures required data exists.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultUserInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    @Value("${app.default-users.create:true}")
    private boolean createDefaultUsers;
    
    @Value("${app.developer.email:developer@example.com}")
    private String developerEmail;
    
    @Value("${app.developer.password:developer123}")
    private String developerPassword;
    
    @Value("${app.user.email:user@example.com}")
    private String userEmail;
    
    @Value("${app.user.password:user123}")
    private String userPassword;

    @Override
    public void run(String... args) {
        log.info("Initializing default roles and users...");

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
        
        // Only create default users if enabled in config
        if (createDefaultUsers) {
            // Create default users if they don't exist
            createDefaultAdminUser();
            createDefaultDeveloperUser();
            createDefaultRegularUser();
        } else {
            log.info("Default user creation is disabled");
        }

        log.info("Role and user initialization completed");
    }
    
    /**
     * Creates a default admin user if no admin exists.
     */
    private void createDefaultAdminUser() {
        // Skip if user already exists
        if (userRepository.existsByUsername(adminEmail)) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }
        
        // Check if admin role exists
        Optional<Role> adminRole = roleRepository.findByName(RoleEnum.ADMIN);
        if (!adminRole.isPresent()) {
            log.error("Admin role not found, cannot create admin user");
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
    
    /**
     * Creates a default developer user if none exists.
     */
    private void createDefaultDeveloperUser() {
        // Skip if user already exists
        if (userRepository.existsByUsername(developerEmail)) {
            log.info("Developer user already exists: {}", developerEmail);
            return;
        }
        
        // Check if developer role exists
        Optional<Role> developerRole = roleRepository.findByName(RoleEnum.DEVELOPER);
        if (!developerRole.isPresent()) {
            log.error("Developer role not found, cannot create developer user");
            return;
        }
        
        // Create new developer user
        UserEntity developerUser = new UserEntity();
        developerUser.setUsername(developerEmail);
        developerUser.setPassword(passwordEncoder.encode(developerPassword));
        developerUser.setRoles(Collections.singletonList(developerRole.get()));
        
        userRepository.save(developerUser);
        log.info("Created default developer user: {}", developerEmail);
    }
    
    /**
     * Creates a default regular user if none exists.
     */
    private void createDefaultRegularUser() {
        // Skip if user already exists
        if (userRepository.existsByUsername(userEmail)) {
            log.info("Regular user already exists: {}", userEmail);
            return;
        }
        
        // Check if user role exists
        Optional<Role> userRole = roleRepository.findByName(RoleEnum.USER);
        if (!userRole.isPresent()) {
            log.error("User role not found, cannot create regular user");
            return;
        }
        
        // Create new regular user
        UserEntity regularUser = new UserEntity();
        regularUser.setUsername(userEmail);
        regularUser.setPassword(passwordEncoder.encode(userPassword));
        regularUser.setRoles(Collections.singletonList(userRole.get()));
        
        userRepository.save(regularUser);
        log.info("Created default regular user: {}", userEmail);
    }
}