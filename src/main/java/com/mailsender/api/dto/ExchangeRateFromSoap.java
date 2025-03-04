package com.mailsender.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateFromSoap {
    private BigDecimal sellRate;
    private BigDecimal buyRate;
}
