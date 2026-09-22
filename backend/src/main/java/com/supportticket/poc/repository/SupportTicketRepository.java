package com.supportticket.poc.repository;

import com.supportticket.poc.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(String status);

    List<SupportTicket>
    findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
            String title,
            String description
    );

    List<SupportTicket>
    findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
            String firstStatus,
            String title,
            String secondStatus,
            String description
    );
}
