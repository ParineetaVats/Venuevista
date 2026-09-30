package com.venuevista.controller;

import com.venuevista.dto.TicketResponse;
import com.venuevista.security.UserPrincipal;
import com.venuevista.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final BookingService bookingService;

    public TicketController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getMyTickets(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getUserTickets(principal.getId()));
    }
}
