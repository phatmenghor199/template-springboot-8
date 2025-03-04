package com.mailsender.api.repository;

import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.models.Branch;
import com.mailsender.api.models.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByName(String name);
    Page<Company> findByStatus(StatusData status, Pageable pageable);
    Page<Company> findByNameContainingIgnoreCaseAndStatus(String name, StatusData status, Pageable pageable);
    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
