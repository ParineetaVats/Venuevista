package com.venuevista.exception;

public class BookingException extends RuntimeException {

    private final String errorCode;
    private final int statusCode;

    public BookingException(String errorCode, String message) {
        this(errorCode, message, 400);
    }

    public BookingException(String errorCode, String message, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
