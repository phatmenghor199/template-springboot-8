package com.mailsender.api.service;

import com.mailsender.api.dto.ExchangeDashboardResponseDto;
import com.mailsender.api.dto.ExchangeRateResponseDto;
import com.mailsender.api.models.ExchangeDashboard;

import java.util.List;

public interface ExchangeDashboardService {
    List<ExchangeDashboardResponseDto> getLast30DaysExchangeRates(String fromDateStr, String toDateStr);
}
