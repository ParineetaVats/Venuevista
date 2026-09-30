package com.venuevista.controller;

import com.venuevista.dto.VenueRequest;
import com.venuevista.dto.VenueResponse;
import com.venuevista.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/venues")
public class AdminVenueController {

    private final AdminService adminService;

    public AdminVenueController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        VenueResponse response = adminService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
        VenueResponse response = adminService.updateVenue(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        adminService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
