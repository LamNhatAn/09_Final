package com.utetra.service;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Email da duoc su dung: " + email);
    }
}
