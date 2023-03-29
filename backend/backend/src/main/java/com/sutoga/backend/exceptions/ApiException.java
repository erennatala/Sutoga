package com.sutoga.backend.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
public class ApiException {
    private String msg;

    private HttpStatus httpStatus;

    private ZonedDateTime timestamp;
    
    public ApiException(String msg, HttpStatus httpStatus, ZonedDateTime timestamp) {
        this.msg = msg;
        this.httpStatus = httpStatus;
        this.timestamp = timestamp;
    }
}
