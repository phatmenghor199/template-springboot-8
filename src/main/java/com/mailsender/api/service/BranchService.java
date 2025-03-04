package com.mailsender.api.service;

import com.mailsender.api.dto.AllBranchResponseDto;
import com.mailsender.api.dto.BranchResponseDto;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.request.BranchRequestDto;

import java.util.Optional;

public interface BranchService {
    BranchResponseDto createBranch(BranchRequestDto branchRequestDto);

    BranchResponseDto getBranchById(Long id);

    AllBranchResponseDto getAllBranch(Optional<StatusData> status, int pageNo, int pageSize,String search);

    BranchResponseDto updateBranch(Long id, BranchRequestDto branchRequestDto);
}
