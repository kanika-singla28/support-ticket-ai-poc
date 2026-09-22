package com.supportticket.poc.exception;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(Long ticketId) {
        super("Support ticket not found: " + ticketId);
    }
}
