package com.venuevista.controller;

import com.venuevista.dto.EventSalesResponse;
import com.venuevista.dto.SalesSummaryResponse;
import com.venuevista.entity.BookingStatus;
import com.venuevista.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/sales")
public class AdminSalesController {

    private final AdminService adminService;

    public AdminSalesController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/summary")
    public ResponseEntity<SalesSummaryResponse> getSalesSummary() {
        return ResponseEntity.ok(adminService.getSalesSummary());
    }

    @GetMapping
    public ResponseEntity<List<EventSalesResponse>> getSalesList(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(adminService.getSalesList(eventId, venueId, status, from, to));
    }
}
