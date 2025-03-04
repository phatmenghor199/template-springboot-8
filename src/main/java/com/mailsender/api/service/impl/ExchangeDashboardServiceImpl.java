package com.mailsender.api.service.impl;

import com.mailsender.api.dto.ExchangeDashboardResponseDto;
import com.mailsender.api.dto.ExchangeRateResponseDto;
import com.mailsender.api.mapper.CompanyMapper;
import com.mailsender.api.mapper.ExchangeRateMapper;
import com.mailsender.api.models.ExchangeDashboard;
import com.mailsender.api.repository.ExchangeDashboardRepository;
import com.mailsender.api.service.ExchangeDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeDashboardServiceImpl implements ExchangeDashboardService {

    private final ExchangeDashboardRepository exchangeDashboardRepository;
    private final ExchangeRateMapper exchangeRateMapper;


    @Override
    public List<ExchangeDashboardResponseDto> getLast30DaysExchangeRates(String fromDateStr, String toDateStr) {
        LocalDate endDate;
        LocalDate startDate;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (fromDateStr != null && toDateStr != null) {
            // Use custom date range from the client
            try {
                startDate = LocalDate.parse(fromDateStr, formatter);
                endDate = LocalDate.parse(toDateStr, formatter);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd.");
            }
        } else {
            // Default to last 30 days
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        List<ExchangeDashboard> exchangeData = exchangeDashboardRepository.findExchangeDashboardsByFetchedAtBetween(startDate, endDate);
        return exchangeRateMapper.toDtoDashboard(exchangeData);
    }
}
