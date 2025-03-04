package com.mailsender.api.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CustomerScheduleDto {
    private UUID id;
    private List<String> sendDays;
    private String sendTime;
}
