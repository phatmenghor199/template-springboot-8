package com.bkgoutchecker.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BakongResponse {
    private String trxref;
    private String trxHash;
    private double amount;
    private String currency;
    private String senderBank;
    private String receiverBank;
    private String createTime;
}
