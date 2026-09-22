package com.supportticket.poc.controller;

import com.supportticket.poc.dto.ApiResponse;
import com.supportticket.poc.dto.CreateTicketRequest;
import com.supportticket.poc.dto.TicketResponse;
import com.supportticket.poc.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> create(
            @Valid @RequestBody CreateTicketRequest request) {

        TicketResponse created = supportTicketService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }
}
