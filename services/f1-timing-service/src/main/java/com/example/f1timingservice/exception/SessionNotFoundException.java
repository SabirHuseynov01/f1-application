package com.example.f1timingservice.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(Integer sessionKey) {
        super("Session not found: " + sessionKey);
    }
}
