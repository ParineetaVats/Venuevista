package com.venuevista.controller;

import com.venuevista.dto.*;
import com.venuevista.entity.SeatType;
import com.venuevista.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminSeatingController {

    private final AdminService adminService;

    public AdminSeatingController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/venues/{venueId}/seats")
    public ResponseEntity<AdminSeatingResponse> getVenueSeating(
            @PathVariable Long venueId,
            @RequestParam(required = false) Long eventId
    ) {
        return ResponseEntity.ok(adminService.getVenueSeating(venueId, eventId));
    }

    @PostMapping("/venues/{venueId}/seats")
    public ResponseEntity<AdminSeatingResponse> addSeats(
            @PathVariable Long venueId,
            @RequestBody AddSeatRequest request
    ) {
        return ResponseEntity.ok(adminService.addSeats(venueId, request));
    }

    @PutMapping("/seats/{seatId}")
    public ResponseEntity<AdminSeatDTO> updateSeat(
            @PathVariable Long seatId,
            @RequestBody UpdateSeatRequest request
    ) {
        return ResponseEntity.ok(adminService.updateSeat(seatId, request));
    }

    @PutMapping("/seats/{seatId}/availability")
    public ResponseEntity<AdminSeatDTO> updateSeatAvailability(
            @PathVariable Long seatId,
            @RequestBody Map<String, Boolean> body
    ) {
        Boolean active = body.get("active");
        return ResponseEntity.ok(adminService.updateSeat(seatId, new UpdateSeatRequest(null, active)));
    }

    @PutMapping("/seats/{seatId}/type")
    public ResponseEntity<AdminSeatDTO> updateSeatType(
            @PathVariable Long seatId,
            @RequestBody Map<String, String> body
    ) {
        SeatType seatType = SeatType.fromValue(body.get("seatType"));
        return ResponseEntity.ok(adminService.updateSeat(seatId, new UpdateSeatRequest(seatType, null)));
    }

    @DeleteMapping("/seats/{seatId}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long seatId) {
        adminService.deleteSeat(seatId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/venues/{venueId}/rows/{rowLabel}/type")
    public ResponseEntity<AdminSeatingResponse> updateRowType(
            @PathVariable Long venueId,
            @PathVariable String rowLabel,
            @RequestBody Map<String, String> body
    ) {
        SeatType seatType = SeatType.fromValue(body.get("seatType"));
        return ResponseEntity.ok(adminService.applyRowType(venueId, rowLabel, seatType));
    }

    @PostMapping("/venues/{venueId}/seating/reset")
    public ResponseEntity<AdminSeatingResponse> resetSeating(@PathVariable Long venueId) {
        return ResponseEntity.ok(adminService.resetSeatingLayout(venueId));
    }
}
