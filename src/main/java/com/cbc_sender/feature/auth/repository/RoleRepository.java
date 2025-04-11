package com.cbc_sender.feature.auth.repository;

import com.cbc_sender.enumation.RoleEnum;
import com.cbc_sender.feature.auth.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
    boolean existsByName(RoleEnum name);
}
