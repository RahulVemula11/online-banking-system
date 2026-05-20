package com.rahul.banking.exception;

/**
 * A simple exception that also knows which HTTP status it should produce.
 * Throw this anywhere in the service layer; the handler below turns it into a clean JSON error.
 */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() { return status; }
}
