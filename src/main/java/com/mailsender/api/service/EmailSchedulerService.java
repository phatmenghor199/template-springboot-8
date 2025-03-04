package com.mailsender.api.service;

import com.mailsender.api.dto.*;
import com.mailsender.api.enumation.Status;
import com.mailsender.api.models.ExchangeRate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmailSchedulerService {
    AllEmailSchedulerResponseDto getAllHistory(Status status, LocalDate date, String search, int pageNo, int pageSize);
    EmailSchedulerResponseDto getEmailSchedulerById(UUID id);
    boolean resendEmailByHistoryId(UUID historyId);
    boolean sendEmailByCustomerId(Long customerId);
    ExchangeRateDto getLatestExchangeRate();
}
