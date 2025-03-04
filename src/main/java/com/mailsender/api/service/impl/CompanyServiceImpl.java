package com.mailsender.api.service.impl;

import com.mailsender.api.dto.AllCompanyResponseDto;
import com.mailsender.api.dto.CompanyResponseDto;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.exceptions.DuplicateNameException;
import com.mailsender.api.exceptions.NotFoundException;
import com.mailsender.api.mapper.CompanyMapper;
import com.mailsender.api.models.Company;
import com.mailsender.api.repository.CompanyRepository;
import com.mailsender.api.request.CompanyRequestDto;
import com.mailsender.api.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    public CompanyResponseDto createCompany(CompanyRequestDto companyRequestDto) {

        if (companyRepository.existsByName(companyRequestDto.getName())) {
            log.warn("Company name '{}' already exists", companyRequestDto.getName());
            throw new DuplicateNameException("Company name '" + companyRequestDto.getName() + "' already exists");
        }

        log.info("Creating new company with name: {}", companyRequestDto.getName());
        Company branch = companyMapper.toEntity(companyRequestDto);
        Company savedBranch = companyRepository.save(branch);
        log.info("Company created with ID: {}", savedBranch.getId());
        return companyMapper.toDto(savedBranch);
    }

    @Override
    public CompanyResponseDto getBranchById(Long id) {
        log.info("Fetching branch with ID: {}", id);
        Company company = companyRepository.findById(id).orElseThrow(() -> {
            log.error("Company not found with ID: {}", id);
            return new NotFoundException("Company not found with ID:" + id);
        });
        log.info("Company found: {}", company.toString());
        return companyMapper.toDto(company);
    }

    @Override
    public AllCompanyResponseDto getAllCompany(Optional<StatusData> status, int pageNo, int pageSize, String search) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Company> companyPage;

        if (status.isPresent() && (search != null && !search.isEmpty())) {
            log.info("Fetching company with status: {} and name containing: {}", status.get(), search);
            companyPage = companyRepository.findByNameContainingIgnoreCaseAndStatus(search, status.get(), pageable);
        } else if (status.isPresent()) {
            log.info("Fetching company with status: {}", status.get());
            companyPage = companyRepository.findByStatus(status.get(), pageable);
        } else if (search != null && !search.isEmpty()) {
            log.info("Fetching company with name containing: {}", search);
            companyPage = companyRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            log.info("Fetching all company");
            companyPage = companyRepository.findAll(pageable);
        }

        List<CompanyResponseDto> companyResponseDto = companyPage.getContent()
                .stream()
                .map(companyMapper::toDto) // assuming branchMapper converts BranchEntity to BranchDto
                .collect(Collectors.toList());

        log.info("Company founds: {}", companyResponseDto);
        return companyMapper.mapToListDto(companyResponseDto, companyPage);
    }

    @Override
    public CompanyResponseDto updateBranch(Long id, CompanyRequestDto companyRequestDto) {
        log.info("Updating company with ID: {}", id);
        Optional<Company> existingCompany = companyRepository.findById(id);
        if (existingCompany.isPresent()) {

            Company currentCompany = existingCompany.get();
            // Check if the name is being updated
            if (!currentCompany.getName().equals(companyRequestDto.getName())) {
                // Check if another branch already has the same name
                if (companyRepository.existsByName(companyRequestDto.getName())) {
                    log.warn("Company with name '{}' already exists. Cannot update.", companyRequestDto.getName());
                    throw new DuplicateNameException("Company with name '" + companyRequestDto.getName() + "' already exists. Cannot update.");
                }
            }

            Company company = companyMapper.toEntity(companyRequestDto);
            company.setId(id);
            Company updatedCompany = companyRepository.save(company);
            log.info("Company updated: {}", updatedCompany);
            return companyMapper.toDto(updatedCompany);
        } else {
            log.warn("Company with ID {} not found for update", id);
            throw new NotFoundException("Company with ID '" + companyRequestDto.getName() + "' is not found for update.");
        }
    }

}
