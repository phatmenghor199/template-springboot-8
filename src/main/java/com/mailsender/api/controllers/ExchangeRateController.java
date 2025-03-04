package com.mailsender.api.controllers;

import com.mailsender.api.dto.ExchangeDashboardResponseDto;
import com.mailsender.api.dto.ExchangeRateDto;
import com.mailsender.api.dto.ExchangeRateResponseDto;
import com.mailsender.api.exceptions.ApiResponse;
import com.mailsender.api.models.ExchangeRate;
import com.mailsender.api.service.EmailSchedulerService;
import com.mailsender.api.service.ExchangeDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exchange-rate")
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateController {
    private final EmailSchedulerService emailSchedulerService;  // Ensure your service interface includes getLatestExchangeRate()
    private final ExchangeDashboardService exchangeRateService;

    @PostMapping
    public ApiResponse<ExchangeRateDto> getExchangeRate() {
        log.info("GET /api/v1/exchange-rate called");
        ExchangeRateDto exchangeRate = emailSchedulerService.getLatestExchangeRate();
        return new ApiResponse<>("success", "Exchange rate response successfully", exchangeRate);
    }

    @PostMapping("/last-30-days")
    public ApiResponse<List<ExchangeDashboardResponseDto>> getLast30DaysExchangeRates(@RequestParam(value = "fromDate", required = false) String fromDateStr,
                                                                                      @RequestParam(value = "toDate", required = false) String toDateStr) {
        List<ExchangeDashboardResponseDto> exchangeRates = exchangeRateService.getLast30DaysExchangeRates(fromDateStr, toDateStr);
        return new ApiResponse<>("success", "Exchange rate dashboard response successfully", exchangeRates);
    }
}
