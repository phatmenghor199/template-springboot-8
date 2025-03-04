package com.mailsender.api.request;

import com.mailsender.api.enumation.StatusData;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CompanyRequestDto {
    @NotBlank(message = "Name is required")
    private String name;
    @NotNull(message = "Status is required")
    private StatusData status;
}
