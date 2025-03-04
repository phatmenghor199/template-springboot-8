package com.mailsender.api.request;

import com.mailsender.api.enumation.StatusData;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CustomerRequestDto {
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "companyId is required")
    private Long companyId;
    @NotNull(message = "branchId is required")
    private Long branchId;
    @NotNull(message = "Status is required")
    private StatusData status;

    private List<CustomerScheduleRequestDto> schedules;
}
