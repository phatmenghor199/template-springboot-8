package com.mailsender.api.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExchangeRateRequestDto {
    private BigDecimal buyRateThb;
    private BigDecimal sellRateThb;
    private BigDecimal buyRateUsd;
    private BigDecimal sellRateUsd;
    private LocalDate rateDate;
}
