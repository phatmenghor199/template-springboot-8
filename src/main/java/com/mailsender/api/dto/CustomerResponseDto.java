package com.mailsender.api.dto;

import com.mailsender.api.enumation.StatusData;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerResponseDto {
    private Long id;
    private String username;
    private String email;
    private CompanyResponseDto company;
    private BranchResponseDto branch;
    private StatusData status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CustomerScheduleDto> schedules;
}
