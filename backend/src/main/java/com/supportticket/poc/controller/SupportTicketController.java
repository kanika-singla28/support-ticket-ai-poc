package com.supportticket.poc.controller;

import com.supportticket.poc.dto.AddCommentRequest;
import com.supportticket.poc.dto.ApiResponse;
import com.supportticket.poc.dto.CreateTicketRequest;
import com.supportticket.poc.dto.TicketResponse;
import com.supportticket.poc.dto.TransitionStatusRequest;
import com.supportticket.poc.dto.UpdateTicketRequest;
import com.supportticket.poc.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(
            SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> create(
            @Valid @RequestBody CreateTicketRequest request) {

        TicketResponse created =
                supportTicketService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        supportTicketService.list(search, status)
                )
        );
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> getById(
            @PathVariable Long ticketId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        supportTicketService.getById(ticketId)
                )
        );
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> update(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        supportTicketService.update(ticketId, request)
                )
        );
    }

    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<ApiResponse<TicketResponse>> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddCommentRequest request) {

        TicketResponse updated =
                supportTicketService.addComment(ticketId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(updated));
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> transitionStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody TransitionStatusRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        supportTicketService.transitionStatus(
                                ticketId,
                                request.status()
                        )
                )
        );
    }
}
