package com.cbc_sender.exceptions.response;

import lombok.Data;

import java.util.Date;

@Data
public class ErrorObject {
    private int statusCode;
    private String message;
    private Date timestamp;
}
