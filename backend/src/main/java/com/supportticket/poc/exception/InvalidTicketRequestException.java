package com.supportticket.poc.exception;

public class InvalidTicketRequestException extends RuntimeException {

    public InvalidTicketRequestException(String message) {
        super(message);
    }
}
