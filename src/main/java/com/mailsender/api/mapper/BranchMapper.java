package com.mailsender.api.mapper;

import com.mailsender.api.dto.AllBranchResponseDto;
import com.mailsender.api.dto.BranchResponseDto;
import com.mailsender.api.models.Branch;
import com.mailsender.api.request.BranchRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BranchMapper {
    BranchMapper INSTANCE = Mappers.getMapper(BranchMapper.class);

    Branch toEntity(BranchRequestDto dto);
    BranchResponseDto toDto(Branch entity);

    default AllBranchResponseDto mapToListDto(List<BranchResponseDto> content, Page<Branch> branchPage) {
        AllBranchResponseDto branchResponse = new AllBranchResponseDto();
        branchResponse.setContent(content);
        branchResponse.setPageNo(branchPage.getNumber() + 1); // Convert 0-indexed to 1-indexed page number
        branchResponse.setPageSize(branchPage.getSize());
        branchResponse.setTotalElements(branchPage.getTotalElements());
        branchResponse.setTotalPages(branchPage.getTotalPages());
        branchResponse.setLast(branchPage.isLast());
        return branchResponse;
    }
}
