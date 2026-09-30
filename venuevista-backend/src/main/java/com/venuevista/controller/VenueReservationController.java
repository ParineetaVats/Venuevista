package com.venuevista.controller;

import com.venuevista.dto.VenueReservationResponse;
import com.venuevista.security.UserPrincipal;
import com.venuevista.service.VenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/venue-reservations")
public class VenueReservationController {

    private final VenueService venueService;

    public VenueReservationController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<VenueReservationResponse>> getMyReservations(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(venueService.getUserReservations(principal.getId()));
    }
}
