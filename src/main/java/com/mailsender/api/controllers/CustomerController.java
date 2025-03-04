package com.mailsender.api.controllers;

import com.mailsender.api.dto.*;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.exceptions.ApiResponse;
import com.mailsender.api.request.CustomerGetAllRequestDto;
import com.mailsender.api.request.CustomerRequestDto;
import com.mailsender.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ApiResponse<CustomerResponseDto> createBranch(@Valid @RequestBody CustomerRequestDto customerRequestDto) {
        log.info("API Request: Create Customer is call- {}", customerRequestDto.toString());
        CustomerResponseDto customerResponseDto = customerService.createCustomer(customerRequestDto);
        return new ApiResponse<>("success", "Customer created successfully", customerResponseDto);
    }

    @PostMapping("/{id}")
    public ApiResponse<CustomerResponseDto> getCustomerById(@PathVariable Long id) {
        log.info("API Request: Get Customer by ID call - {}", id);
        CustomerResponseDto customerResponseDto = customerService.getCustomerById(id);
        return new ApiResponse<>("success", "Customer by id response successfully", customerResponseDto);
    }

    @PostMapping("update/{id}")
    public ApiResponse<CustomerResponseDto> updateCustomer(@PathVariable Long id, @RequestBody CustomerRequestDto customerUpdateDto) {
        log.info("API Request: Update Customer call - ID: {}", id);
        CustomerResponseDto updatedCustomer = customerService.updateCustomer(id, customerUpdateDto);
        log.info("Customer Updated: {}", updatedCustomer.toString());
        return new ApiResponse<>("success", "Customer updated successfully", updatedCustomer);
    }

    @PostMapping("all")
    public ApiResponse<AllCustomerResponseDto> getAllCustomer(
            @RequestBody CustomerGetAllRequestDto requestDto) {

        log.info("API Request: Get All Customer call - ID: {}", requestDto.toString());
        Optional<StatusData> statusOpt = Optional.empty();
        if (requestDto.getPageNo() <= 0) {
            // If pageNo is invalid (e.g., 0 or negative), return an error response
            return new ApiResponse<>("error", "Page number must be greater than 0", null);
        }

        if (requestDto.getPageSize() <= 0) {
            // If pageSize is invalid (e.g., 0 or negative), return an error response
            return new ApiResponse<>("error", "Page size must be greater than 0", null);
        }

        AllCustomerResponseDto customerResponseDto = customerService.getAllCustomer(
                requestDto.getStatus(),
                requestDto.getCompanyIds(),
                requestDto.getBranchIds(),
                requestDto.getSearch(),
                requestDto.getPageNo(), requestDto.getPageSize());
        return new ApiResponse<>("success", "Customer retrieved successfully", customerResponseDto);
    }

}
