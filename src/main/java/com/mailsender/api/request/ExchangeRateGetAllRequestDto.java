package com.mailsender.api.request;

import com.mailsender.api.enumation.StatusData;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ExchangeRateGetAllRequestDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private int pageNo = 1; // default value
    private int pageSize = 10; // default value
}
