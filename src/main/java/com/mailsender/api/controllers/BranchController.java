package com.mailsender.api.controllers;

import com.mailsender.api.dto.AllBranchResponseDto;
import com.mailsender.api.dto.BranchResponseDto;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.exceptions.ApiResponse;
import com.mailsender.api.request.BranchRequestDto;
import com.mailsender.api.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Slf4j
public class BranchController {
    private final BranchService branchService;

    @PostMapping
    public ApiResponse<BranchResponseDto> createBranch(@Valid @RequestBody BranchRequestDto branchRequestDto) {
        log.info("API Request: Create Branch is call- {}", branchRequestDto.toString());
        BranchResponseDto branchResponseDto = branchService.createBranch(branchRequestDto);
        return new ApiResponse<>("success", "Branch created successfully", branchResponseDto);
    }

    @PostMapping("/{id}")
    public ApiResponse<BranchResponseDto> getBranchById(@PathVariable Long id) {
        log.info("API Request: Get Branch by ID - {}", id);
        BranchResponseDto branchRequestDto = branchService.getBranchById(id);
        return new ApiResponse<>("success", "Branch by id response successfully", branchRequestDto);
    }

    @PostMapping("/update/{id}")
    public ApiResponse<BranchResponseDto> updateBranch(@PathVariable Long id, @Valid @RequestBody BranchRequestDto branchRequestDto) {
        log.info("API Request: Update Branch - ID: {}", id);
        BranchResponseDto updatedBranch = branchService.updateBranch(id, branchRequestDto);

        log.info("Branch Updated: {}", updatedBranch.getName());
        return new ApiResponse<>("success", "Branch updated successfully", updatedBranch);
    }

    @PostMapping("all")
    public ApiResponse<AllBranchResponseDto> getAllBranches(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(value = "search", required = false) String search) {
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

        AllBranchResponseDto branchResponseDto = branchService.getAllBranch(statusOpt, pageNo - 1, pageSize, search);
        return new ApiResponse<>("success", "Branches retrieved successfully", branchResponseDto);
    }
}
