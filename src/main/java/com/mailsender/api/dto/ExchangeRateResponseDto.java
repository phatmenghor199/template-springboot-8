package com.mailsender.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExchangeRateResponseDto {
    private Long id;
    private BigDecimal buyRateThb;
    private BigDecimal sellRateThb;
    private BigDecimal buyRateUsd;
    private BigDecimal sellRateUsd;
    private LocalDateTime fetchedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
