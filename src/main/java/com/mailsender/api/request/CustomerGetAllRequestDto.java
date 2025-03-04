package com.mailsender.api.request;

import com.mailsender.api.enumation.StatusData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class CustomerGetAllRequestDto {
    private StatusData status;
    private List<Long> companyIds;
    private List<Long> branchIds;
    private String search;
    private int pageNo = 1; // default value
    private int pageSize = 10; // default value
}
