package com.mailsender.api.dto;

import com.mailsender.api.enumation.StatusData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BranchResponseDto {
    private Long id;
    private String name;
    private String location;
    private StatusData status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
