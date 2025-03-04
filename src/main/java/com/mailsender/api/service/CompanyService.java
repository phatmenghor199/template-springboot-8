package com.mailsender.api.service;

import com.mailsender.api.dto.AllBranchResponseDto;
import com.mailsender.api.dto.AllCompanyResponseDto;
import com.mailsender.api.dto.CompanyResponseDto;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.request.CompanyRequestDto;

import java.util.Optional;

public interface CompanyService {
    CompanyResponseDto createCompany(CompanyRequestDto companyRequestDto);

    CompanyResponseDto getBranchById(Long id);

    AllCompanyResponseDto getAllCompany(Optional<StatusData> status, int pageNo, int pageSize, String search);

    CompanyResponseDto updateBranch(Long id, CompanyRequestDto companyRequestDto);
}
