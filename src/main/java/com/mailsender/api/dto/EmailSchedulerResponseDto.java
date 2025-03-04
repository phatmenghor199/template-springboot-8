package com.mailsender.api.dto;

import com.mailsender.api.enumation.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EmailSchedulerResponseDto {
    private UUID id;
    private CustomerResponseDto customer;
    private ExchangeRateResponseDto exchangeRate;
    private Status status;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}