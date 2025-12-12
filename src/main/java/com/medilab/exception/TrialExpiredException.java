package com.medilab.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class TrialExpiredException extends RuntimeException {
    public TrialExpiredException(String message) { super(message); }
}

