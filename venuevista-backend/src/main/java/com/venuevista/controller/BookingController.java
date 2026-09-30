package com.venuevista.controller;

import com.venuevista.dto.*;
import com.venuevista.security.UserPrincipal;
import com.venuevista.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> getQuote(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.ok(bookingService.getQuote(request));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        BookingResponse response = bookingService.createBooking(request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.getUserBookings(principal.getId()));
    }

    @GetMapping("/{ref}")
    public ResponseEntity<BookingResponse> getBookingByRef(
            @PathVariable String ref,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(bookingService.getBookingByReference(ref, principal.getUser()));
    }

    @GetMapping("/{ref}/tickets")
    public ResponseEntity<List<BookingSeatDTO>> getBookingTickets(
            @PathVariable String ref,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        BookingResponse booking = bookingService.getBookingByReference(ref, principal.getUser());
        return ResponseEntity.ok(booking.seats());
    }

    @PutMapping("/{ref}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable String ref,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(bookingService.cancelBooking(ref, principal.getUser()));
    }
}
