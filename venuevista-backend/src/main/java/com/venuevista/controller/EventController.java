package com.venuevista.controller;

import com.venuevista.dto.EventResponse;
import com.venuevista.dto.EventSeatsResponse;
import com.venuevista.dto.HoldRequest;
import com.venuevista.dto.HoldResponse;
import com.venuevista.security.UserPrincipal;
import com.venuevista.service.EventCatalogService;
import com.venuevista.service.SeatHoldService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventCatalogService eventCatalogService;
    private final SeatHoldService seatHoldService;

    public EventController(EventCatalogService eventCatalogService, SeatHoldService seatHoldService) {
        this.eventCatalogService = eventCatalogService;
        this.seatHoldService = seatHoldService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(eventCatalogService.filterEvents(q, category, city, from, featured, sort));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<EventResponse>> getFeaturedEvents() {
        return ResponseEntity.ok(eventCatalogService.getFeaturedEvents());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<EventResponse>> getEventsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(eventCatalogService.getEventsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> searchEvents(@RequestParam(required = false, defaultValue = "") String keyword) {
        return ResponseEntity.ok(eventCatalogService.searchEvents(keyword));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(eventCatalogService.getCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventCatalogService.getEventById(id));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<EventSeatsResponse> getEventSeats(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(eventCatalogService.getEventSeats(id, userId));
    }

    @PostMapping("/{eventId}/holds")
    public ResponseEntity<HoldResponse> createHold(
            @PathVariable Long eventId,
            @Valid @RequestBody HoldRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        HoldResponse response = seatHoldService.createHold(eventId, request.seatIds(), principal.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}/holds")
    public ResponseEntity<Void> releaseHolds(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        seatHoldService.releaseUserHolds(eventId, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
