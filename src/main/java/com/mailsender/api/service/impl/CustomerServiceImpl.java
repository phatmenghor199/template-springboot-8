package com.mailsender.api.service.impl;

import com.mailsender.api.dto.AllCustomerResponseDto;
import com.mailsender.api.dto.CustomerResponseDto;
import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.exceptions.DuplicateNameException;
import com.mailsender.api.exceptions.NotFoundException;
import com.mailsender.api.mapper.CustomerMapper;
import com.mailsender.api.models.Branch;
import com.mailsender.api.models.Company;
import com.mailsender.api.models.Customer;
import com.mailsender.api.models.CustomerSchedule;
import com.mailsender.api.repository.BranchRepository;
import com.mailsender.api.repository.CompanyRepository;
import com.mailsender.api.repository.CustomerRepository;
import com.mailsender.api.request.CustomerRequestDto;
import com.mailsender.api.request.CustomerScheduleRequestDto;
import com.mailsender.api.service.CustomerService;
import com.mailsender.api.utils.filter.CustomerSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto) {
        log.info("Creating new Customer with name: {}", customerRequestDto.toString());

        if (customerRepository.existsByEmail(customerRequestDto.getEmail())) {
            log.warn("Customer email '{}' already exists", customerRequestDto.getEmail());
            throw new DuplicateNameException("Customer email '" + customerRequestDto.getEmail() + "' already exists");
        }

        // Validate the company ID
        Company company = companyRepository.findById(customerRequestDto.getCompanyId())
                .orElseThrow(() -> {
                    log.error("Customer company ID not found with ID: {}", customerRequestDto.getCompanyId());
                    return new IllegalArgumentException("Invalid company ID: " + customerRequestDto.getCompanyId());
                });

        // Validate the branch ID
        Branch branch = branchRepository.findById(customerRequestDto.getBranchId())
                .orElseThrow(() -> {
                    log.error("Customer brand ID not found with ID: {}", customerRequestDto.getBranchId());
                    return new IllegalArgumentException("Invalid branch ID: " + customerRequestDto.getBranchId());
                });

        Customer customer = customerMapper.toEntity(customerRequestDto);
        customer.setCompany(company);
        customer.setBranch(branch);

        // Save the customer to the database to generate the ID
        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        log.info("Customer created with ID: {}", savedCustomer.getId());
        return customerMapper.toDto(savedCustomer);
    }

    @Override
    public CustomerResponseDto getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer = customerRepository.findById(id).orElseThrow(() -> {
            log.error("Customer not found with ID: {}", id);
            return new NotFoundException("Customer not found with ID:" + id);
        });

        CustomerResponseDto customerResponseDto = customerMapper.toDto(customer);
        log.info("Customer found customerResponseDto: {}", customerResponseDto.toString());
        return customerResponseDto;
    }

    @Transactional
    @Override
    public CustomerResponseDto updateCustomer(Long customerId, CustomerRequestDto customerUpdateDto) {
        log.info("Attempting to update customer with ID: {}", customerId);

        // Retrieve the existing customer
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("Customer with ID {} not found", customerId);
                    return new RuntimeException("Customer not found");
                });

        // Update the customer's details
        existingCustomer.setUsername(customerUpdateDto.getUsername());
        existingCustomer.setEmail(customerUpdateDto.getEmail());
        existingCustomer.setStatus(customerUpdateDto.getStatus());

        // Update company and branch if provided
        if (customerUpdateDto.getCompanyId() != null) {
            Company company = companyRepository.findById(customerUpdateDto.getCompanyId())
                    .orElseThrow(() -> {
                        log.error("Company with ID {} not found", customerUpdateDto.getCompanyId());
                        return new NotFoundException("Company not found");
                    });
            existingCustomer.setCompany(company);
        }

        if (customerUpdateDto.getBranchId() != null) {
            Branch branch = branchRepository.findById(customerUpdateDto.getBranchId())
                    .orElseThrow(() -> {
                        log.error("Branch with ID {} not found", customerUpdateDto.getBranchId());
                        return new NotFoundException("Branch not found");
                    });
            existingCustomer.setBranch(branch);
        }

        // Handle schedules: Clear existing ones and add new ones
        if (customerUpdateDto.getSchedules() != null) {
            // Clear existing schedules
            existingCustomer.getSchedules().clear();

            // Add the updated schedules
            // Add the updated schedules, filtering out null values
            customerUpdateDto.getSchedules().stream()
                    .filter(Objects::nonNull)  // Filter out null schedules
                    .forEach(scheduleRequest -> {
                        CustomerSchedule schedule = createScheduleForCustomer(existingCustomer, scheduleRequest);
                        if (schedule != null) {
                            existingCustomer.getSchedules().add(schedule);
                        }
                    });
        }
        Customer updatedCustomer = customerRepository.save(existingCustomer);

        log.info("Customer with ID {} updated successfully", customerId);
        return customerMapper.toDto(updatedCustomer);
    }

    @Override
    public AllCustomerResponseDto getAllCustomer(StatusData status, List<Long> companyIds, List<Long> branchIds, String search, int pageNo, int pageSize) {

        Specification<Customer> spec = Specification.where(CustomerSpecifications.hasStatus(status))
                .and(CustomerSpecifications.inCompanyIds(companyIds))
                .and(CustomerSpecifications.inBranchIds(branchIds))
                .and(CustomerSpecifications.hasUsernameOrEmail(search));;

        log.info("Customer customer filter founds: {}", spec);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<Customer> customerPage = customerRepository.findAll(spec, pageable);

        List<CustomerResponseDto> customerResponseDto = customerMapper.toDtoList(customerPage.getContent());

        log.info("Customer responseDto founds: {}", customerResponseDto);
        return customerMapper.mapToListDto(customerResponseDto, customerPage);
    }

    private CustomerSchedule createScheduleForCustomer(Customer existingCustomer, CustomerScheduleRequestDto scheduleRequest) {
        // Remove duplicate days by converting to a Set and back to a List
        List<String> uniqueDays = new ArrayList<>(new HashSet<>(scheduleRequest.getSendDays()));

        // Check if the same combination of sendDays and sendTime already exists
        boolean scheduleExists = existingCustomer.getSchedules().stream()
                .anyMatch(existingSchedule -> existingSchedule.getSendTime().equals(scheduleRequest.getSendTime()) &&
                        existingSchedule.getSendDays().equals(uniqueDays));

        // If the schedule doesn't already exist, create and return it
        if (!scheduleExists) {
            CustomerSchedule schedule = new CustomerSchedule();
            schedule.setSendDays(uniqueDays);
            schedule.setSendTime(scheduleRequest.getSendTime());
            schedule.setCustomer(existingCustomer);
            return schedule;
        }
        return null;  // Return null if the schedule already exists
    }

}
