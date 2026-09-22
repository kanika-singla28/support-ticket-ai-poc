package com.supportticket.poc.controller;

import com.supportticket.poc.dto.ApiResponse;
import com.supportticket.poc.dto.AskAiRequest;
import com.supportticket.poc.dto.AskAiResponse;
import com.supportticket.poc.service.SupportTicketAiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class SupportTicketAiController {

    private final SupportTicketAiService supportTicketAiService;

    public SupportTicketAiController(
            SupportTicketAiService supportTicketAiService) {
        this.supportTicketAiService = supportTicketAiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<AskAiResponse>> ask(
            @Valid @RequestBody AskAiRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        supportTicketAiService.ask(request.question())
                )
        );
    }
}
