package com.roms.exception;

/**
 * Exception thrown when business rules or validation logic fails
 * Used for domain-specific validation errors that are not technical errors
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
