package com.mailsender.api.request;

import com.mailsender.api.enumation.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmailSchedulerAllRequestDto {
    private Status status;
    private LocalDate date;
    private String search;
    private int pageNo = 1; // default value
    private int pageSize = 10; // default value
}
