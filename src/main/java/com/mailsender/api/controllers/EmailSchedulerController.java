package com.mailsender.api.controllers;

import com.mailsender.api.dto.AllEmailSchedulerResponseDto;
import com.mailsender.api.dto.EmailSchedulerResponseDto;
import com.mailsender.api.exceptions.ApiResponse;
import com.mailsender.api.request.EmailSchedulerAllRequestDto;
import com.mailsender.api.service.EmailSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email-scheduler")
@RequiredArgsConstructor
@Slf4j
public class EmailSchedulerController {

    private final EmailSchedulerService emailSchedulerService;

    @PostMapping("/{id}")
    public ApiResponse<EmailSchedulerResponseDto> getEmailSchedulerById(@PathVariable UUID id) {
        log.info("API Request: Get email scheduler by ID call - {}", id);
        EmailSchedulerResponseDto schedulerResponseDto = emailSchedulerService.getEmailSchedulerById(id);
        return new ApiResponse<>("success", "Email scheduler by id response successfully", schedulerResponseDto);
    }

    @PostMapping("/resend-email/{historyId}")
    public ApiResponse<Boolean> resendEmailByHistoryId(@PathVariable UUID historyId) {
        boolean result = emailSchedulerService.resendEmailByHistoryId(historyId);
        return new ApiResponse<>("success", "Email scheduler by id resend successfully", result);
    }

    @PostMapping("/send-email/{customerId}")
    public ApiResponse<Boolean> sendEmailByCustomerId(@PathVariable Long customerId) {
        boolean result = emailSchedulerService.sendEmailByCustomerId(customerId);
        return new ApiResponse<>("success", "Email send to customer by id successfully", result);
    }

    @PostMapping
    public ApiResponse<AllEmailSchedulerResponseDto> getAllScheduler(
            @RequestBody EmailSchedulerAllRequestDto requestDto) {

        log.info("API Request: Get All Email Scheduler call - ID: {}", requestDto.toString());
        if (requestDto.getPageNo() <= 0) {
            // If pageNo is invalid (e.g., 0 or negative), return an error response
            return new ApiResponse<>("error", "Page number must be greater than 0", null);
        }

        if (requestDto.getPageSize() <= 0) {
            // If pageSize is invalid (e.g., 0 or negative), return an error response
            return new ApiResponse<>("error", "Page size must be greater than 0", null);
        }

        AllEmailSchedulerResponseDto customerResponseDto = emailSchedulerService.getAllHistory(
                requestDto.getStatus(),
                requestDto.getDate(),
                requestDto.getSearch(),
                requestDto.getPageNo(),
                requestDto.getPageSize()
        );
        return new ApiResponse<>("success", "Email scheduler retrieved successfully", customerResponseDto);
    }
}
