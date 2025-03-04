package com.mailsender.api.exceptions;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ErrorRequestObject {
    private Integer statusCode;
    private List<Map<String, String>> message;
    private Date timestamp;
}
