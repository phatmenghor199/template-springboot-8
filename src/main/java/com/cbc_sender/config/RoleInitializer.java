package com.cbc_sender.config;

import com.cbc_sender.enumation.RoleEnum;
import com.cbc_sender.feature.auth.models.Role;
import com.cbc_sender.feature.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing default roles...");

        // Create each role if it doesn't already exist
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            if (!roleRepository.existsByName(roleEnum)) {
                Role role = new Role();
                role.setName(roleEnum);
                roleRepository.save(role);
                log.info("Created role: {}", roleEnum);
            }
        });

        log.info("Role initialization completed");
    }
}