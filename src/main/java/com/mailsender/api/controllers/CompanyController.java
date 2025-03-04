package com.mailsender.api.controllers;

import com.mailsender.api.dto.AllBranchResponseDto;
import com.mailsender.api.dto.AllCompanyResponseDto;
import com.mailsender.api.dto.BranchResponseDto;
import com.mailsender.api.dto.CompanyResponseDto;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.exceptions.ApiResponse;
import com.mailsender.api.request.BranchRequestDto;
import com.mailsender.api.request.CompanyRequestDto;
import com.mailsender.api.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    public ApiResponse<CompanyResponseDto> createCompany(@Valid @RequestBody CompanyRequestDto companyRequestDto) {
        log.info("API Request: Create Company is call- {}", companyRequestDto.toString());
        CompanyResponseDto companyResponseDto = companyService.createCompany(companyRequestDto);
        return new ApiResponse<>("success", "Branch created successfully", companyResponseDto);
    }

    @PostMapping("/{id}")
    public ApiResponse<CompanyResponseDto> getCompanyById(@PathVariable Long id) {
        log.info("API Request: Get Company by ID - {}", id);
        CompanyResponseDto companyResponseDto = companyService.getBranchById(id);
        return new ApiResponse<>("success", "Company by id response successfully", companyResponseDto);
    }

    @PostMapping("update/{id}")
    public ApiResponse<CompanyResponseDto> updateCompany(@PathVariable Long id, @Valid @RequestBody CompanyRequestDto companyRequestDto) {
        log.info("API Request: Update Branch - ID: {}", id);
        CompanyResponseDto updatedCompany = companyService.updateBranch(id, companyRequestDto);

        log.info("Company Updated: {}", updatedCompany.getName());
        return new ApiResponse<>("success", "Company updated successfully", updatedCompany);
    }

    @PostMapping("all")
    public ApiResponse<AllCompanyResponseDto> getAllCompany(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(value = "search", required = false) String search
    ) {
        Optional<StatusData> statusOpt = Optional.empty();

        if (pageNo <= 0) {
            // If pageNo is invalid (e.g., 0 or negative), return an error response
            return new ApiResponse<>("error", "Page number must be greater than 0", null);
        }

        if (pageSize <= 0) {
            // If pageSize is invalid (e.g., 0 or negative), return an error response
            return new ApiResponse<>("error", "Page size must be greater than 0", null);
        }

        if (status != null && !status.isEmpty()) {
            try {
                statusOpt = Optional.of(StatusData.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status parameter: {}", status);
            }
        }

        AllCompanyResponseDto companyResponseDto = companyService.getAllCompany(statusOpt, pageNo - 1, pageSize, search);
        return new ApiResponse<>("success", "Company retrieved successfully", companyResponseDto);
    }
}
