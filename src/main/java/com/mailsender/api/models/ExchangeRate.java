package com.mailsender.api.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "exchange_rate")
public class ExchangeRate extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal buyRateThb;
    private BigDecimal sellRateThb;

    private BigDecimal buyRateUsd;
    private BigDecimal sellRateUsd;

    @Column(unique = true)
    private LocalDateTime fetchedAt;
}
