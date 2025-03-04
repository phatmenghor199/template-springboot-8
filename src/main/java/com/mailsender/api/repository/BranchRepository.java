package com.mailsender.api.repository;

import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.models.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Page<Branch> findByStatus(StatusData status, Pageable pageable);
    Page<Branch> findByNameContainingIgnoreCaseAndStatus(String name, StatusData status, Pageable pageable);
    Page<Branch> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByName(String name);
}
