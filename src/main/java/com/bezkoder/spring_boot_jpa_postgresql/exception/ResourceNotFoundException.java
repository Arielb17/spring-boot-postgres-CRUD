package com.bezkoder.spring_boot_jpa_postgresql.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
