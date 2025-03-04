package com.mailsender.api.service;

import com.mailsender.api.dto.*;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.request.CustomerRequestDto;

import java.util.List;

public interface CustomerService {
    CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDTO);

    CustomerResponseDto getCustomerById(Long id);

    CustomerResponseDto updateCustomer(Long customerId, CustomerRequestDto customerUpdateDto);

    AllCustomerResponseDto getAllCustomer(StatusData status, List<Long> companyIds, List<Long> branchIds, String search, int pageNo, int pageSize);
}
