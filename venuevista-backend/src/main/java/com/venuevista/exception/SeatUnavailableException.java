package com.venuevista.exception;

public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) {
        super(message);
    }
    public SeatUnavailableException() {
        super("One or more selected seats are no longer available.");
    }
}
