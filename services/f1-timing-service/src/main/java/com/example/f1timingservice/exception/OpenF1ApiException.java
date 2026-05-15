package com.example.f1timingservice.exception;

public class OpenF1ApiException extends RuntimeException {

    public OpenF1ApiException(String message) {
        super(message);
    }

    public OpenF1ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
