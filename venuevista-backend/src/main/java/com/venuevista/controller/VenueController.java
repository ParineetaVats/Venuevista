package com.venuevista.controller;

import com.venuevista.dto.EventResponse;
import com.venuevista.dto.VenueReservationRequest;
import com.venuevista.dto.VenueReservationResponse;
import com.venuevista.dto.VenueResponse;
import com.venuevista.security.UserPrincipal;
import com.venuevista.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getVenues(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city
    ) {
        return ResponseEntity.ok(venueService.filterVenues(q, type, city));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<EventResponse>> getEventsByVenue(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getEventsByVenueId(id));
    }

    @PostMapping("/{id}/reservations")
    public ResponseEntity<VenueReservationResponse> createReservation(
            @PathVariable Long id,
            @Valid @RequestBody VenueReservationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        VenueReservationResponse response = venueService.createReservation(id, request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
