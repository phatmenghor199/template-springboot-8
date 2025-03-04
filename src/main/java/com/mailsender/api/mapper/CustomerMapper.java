package com.mailsender.api.mapper;

import com.mailsender.api.dto.AllCompanyResponseDto;
import com.mailsender.api.dto.AllCustomerResponseDto;
import com.mailsender.api.dto.CompanyResponseDto;
import com.mailsender.api.dto.CustomerResponseDto;
import com.mailsender.api.models.Company;
import com.mailsender.api.models.Customer;
import com.mailsender.api.request.CustomerRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    Customer toEntity(CustomerRequestDto dto);

    @Mapping(target = "company", source = "company")
    @Mapping(target = "branch", source = "branch")
    @Mapping(target = "schedules", source = "schedules")
    CustomerResponseDto toDto(Customer entity);

    List<CustomerResponseDto> toDtoList(List<Customer> customers);

    default AllCustomerResponseDto mapToListDto(List<CustomerResponseDto> content, Page<Customer> companyPage) {
        AllCustomerResponseDto customerResponseDto = new AllCustomerResponseDto();
        customerResponseDto.setContent(content);
        customerResponseDto.setPageNo(companyPage.getNumber() + 1); // Convert 0-indexed to 1-indexed page number
        customerResponseDto.setPageSize(companyPage.getSize());
        customerResponseDto.setTotalElements(companyPage.getTotalElements());
        customerResponseDto.setTotalPages(companyPage.getTotalPages());
        customerResponseDto.setLast(companyPage.isLast());
        return customerResponseDto;
    }
}
