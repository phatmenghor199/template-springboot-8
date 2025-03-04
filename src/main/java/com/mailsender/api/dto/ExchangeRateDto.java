package com.mailsender.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExchangeRateDto {
    private BigDecimal buyRateUsd;
    private BigDecimal sellRateUsd;
    private BigDecimal buyRateThb;
    private BigDecimal sellRateThb;
    private LocalDateTime fetchedAt;
}
