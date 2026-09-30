package com.venuevista.controller;

import com.venuevista.dto.EventRequest;
import com.venuevista.dto.EventResponse;
import com.venuevista.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final AdminService adminService;

    public AdminEventController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = adminService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        EventResponse response = adminService.updateEvent(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        adminService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
