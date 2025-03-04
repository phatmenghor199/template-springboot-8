package com.mailsender.api.service.impl;

import com.mailsender.api.dto.AllBranchResponseDto;
import com.mailsender.api.dto.BranchResponseDto;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.exceptions.DuplicateNameException;
import com.mailsender.api.exceptions.NotFoundException;
import com.mailsender.api.mapper.BranchMapper;
import com.mailsender.api.models.Branch;
import com.mailsender.api.repository.BranchRepository;
import com.mailsender.api.request.BranchRequestDto;
import com.mailsender.api.service.BranchService;
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
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    public BranchResponseDto createBranch(BranchRequestDto branchRequestDto) {

        if (branchRepository.existsByName(branchRequestDto.getName())) {
            log.warn("Branch name '{}' already exists", branchRequestDto.getName());
            throw new DuplicateNameException("Branch name '" + branchRequestDto.getName() + "' already exists");
        }

        log.info("Creating new branch with name: {}", branchRequestDto.getName());
        Branch branch = branchMapper.toEntity(branchRequestDto);
        Branch savedBranch = branchRepository.save(branch);
        log.info("Branch created with ID: {}", savedBranch.getId());
        return branchMapper.toDto(savedBranch);
    }

    @Override
    public BranchResponseDto getBranchById(Long id) {
        log.info("Fetching branch with ID: {}", id);
        Branch branch = branchRepository.findById(id).orElseThrow(() -> {
            log.error("Branch not found with ID: {}", id);
            return new NotFoundException("Branch not found with ID:" + id);
        });
        log.info("Branch found: {}", branch.toString());
        return branchMapper.toDto(branch);
    }

    @Override
    public AllBranchResponseDto getAllBranch(Optional<StatusData> status, int pageNo, int pageSize, String search) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Branch> branchPage;

        if (status.isPresent() && (search != null && !search.isEmpty())) {
            log.info("Fetching branches with status: {} and name containing: {}", status.get(), search);
            branchPage = branchRepository.findByNameContainingIgnoreCaseAndStatus(search, status.get(), pageable);
        } else if (status.isPresent()) {
            log.info("Fetching branches with status: {}", status.get());
            branchPage = branchRepository.findByStatus(status.get(), pageable);
        } else if (search != null && !search.isEmpty()) {
            log.info("Fetching branches with name containing: {}", search);
            branchPage = branchRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            log.info("Fetching all branches");
            branchPage = branchRepository.findAll(pageable);
        }

        List<BranchResponseDto> content = branchPage.getContent()
                .stream()
                .map(branchMapper::toDto) // assuming branchMapper converts BranchEntity to BranchDto
                .collect(Collectors.toList());
        log.info("Branches found: {}", content);
        return branchMapper.mapToListDto(content, branchPage);
    }

    @Override
    public BranchResponseDto updateBranch(Long id, BranchRequestDto branchRequestDto) {
        log.info("Updating branch with ID: {}", id);
        Optional<Branch> existingBranch = branchRepository.findById(id);
        if (existingBranch.isPresent()) {

            Branch currentBranch = existingBranch.get();
            // Check if the name is being updated
            if (!currentBranch.getName().equals(branchRequestDto.getName())) {
                // Check if another branch already has the same name
                if (branchRepository.existsByName(branchRequestDto.getName())) {
                    log.warn("Branch with name '{}' already exists. Cannot update.", branchRequestDto.getName());
                    throw new DuplicateNameException("Branch with name '" + branchRequestDto.getName() + "' already exists. Cannot update.");
                }
            }

            Branch branch = branchMapper.toEntity(branchRequestDto);
            branch.setId(id);
            Branch updatedBranch = branchRepository.save(branch);
            log.info("Branch updated: {}", updatedBranch);
            return branchMapper.toDto(updatedBranch);
        } else {
            log.warn("Branch with ID {} not found for update", id);
            throw new NotFoundException("Branch with ID '" + branchRequestDto.getName() + "' is not found for update.");
        }
    }
}
