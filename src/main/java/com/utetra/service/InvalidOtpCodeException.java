package com.utetra.service;

public class InvalidOtpCodeException extends RegistrationOtpException {
    public InvalidOtpCodeException(String message) {
        super(message);
    }
}
