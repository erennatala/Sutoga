package com.sutoga.backend.exceptions;
public class ResultNotFoundException extends RuntimeException{
    public ResultNotFoundException(String message) {
        super(message);
    }
}
