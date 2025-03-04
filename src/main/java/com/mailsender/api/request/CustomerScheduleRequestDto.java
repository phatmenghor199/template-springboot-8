package com.mailsender.api.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CustomerScheduleRequestDto {
    private UUID id;
    private List<String> sendDays;
    private String sendTime;
}
