package com.supportticket.poc.exception;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String currentStatus, String requestedStatus) {
        super("Invalid ticket status transition: "
                + currentStatus + " -> " + requestedStatus);
    }
}
