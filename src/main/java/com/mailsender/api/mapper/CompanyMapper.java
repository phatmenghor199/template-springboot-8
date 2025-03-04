package com.mailsender.api.mapper;

import com.mailsender.api.dto.AllCompanyResponseDto;
import com.mailsender.api.dto.CompanyResponseDto;
import com.mailsender.api.models.Company;
import com.mailsender.api.request.CompanyRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    Company toEntity(CompanyRequestDto dto);

    CompanyResponseDto toDto(Company entity);

    default AllCompanyResponseDto mapToListDto(List<CompanyResponseDto> content, Page<Company> companyPage) {
        AllCompanyResponseDto companyResponseDto = new AllCompanyResponseDto();
        companyResponseDto.setContent(content);
        companyResponseDto.setPageNo(companyPage.getNumber() + 1); // Convert 0-indexed to 1-indexed page number
        companyResponseDto.setPageSize(companyPage.getSize());
        companyResponseDto.setTotalElements(companyPage.getTotalElements());
        companyResponseDto.setTotalPages(companyPage.getTotalPages());
        companyResponseDto.setLast(companyPage.isLast());
        return companyResponseDto;
    }
}
