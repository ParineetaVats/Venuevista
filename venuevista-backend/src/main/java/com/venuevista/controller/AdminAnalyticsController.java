package com.venuevista.controller;

import com.venuevista.dto.*;
import com.venuevista.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminAnalyticsController {

    private final AdminService adminService;

    public AdminAnalyticsController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<AnalyticsOverviewResponse> getOverview() {
        return ResponseEntity.ok(adminService.getAnalyticsOverview());
    }

    @GetMapping("/analytics/revenue-over-time")
    public ResponseEntity<List<TimeSeriesResponse>> getRevenueOverTime(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(adminService.getTimeSeriesRevenue(from, to));
    }

    @GetMapping("/analytics/tickets-over-time")
    public ResponseEntity<List<TimeSeriesResponse>> getTicketsOverTime(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(adminService.getTimeSeriesTickets(from, to));
    }

    @GetMapping("/analytics/revenue-by-event")
    public ResponseEntity<List<NamedRevenueResponse>> getRevenueByEvent() {
        return ResponseEntity.ok(adminService.getRevenueByEvent());
    }

    @GetMapping("/analytics/revenue-by-venue")
    public ResponseEntity<List<NamedRevenueResponse>> getRevenueByVenue() {
        return ResponseEntity.ok(adminService.getRevenueByVenue());
    }

    @GetMapping("/analytics/ticket-category-distribution")
    public ResponseEntity<List<CategoryDistributionResponse>> getCategoryDistribution() {
        return ResponseEntity.ok(adminService.getTicketCategoryDistribution());
    }

    @GetMapping("/analytics/occupancy-by-event")
    public ResponseEntity<List<EventOccupancyResponse>> getOccupancyByEvent() {
        return ResponseEntity.ok(adminService.getOccupancyByEvent());
    }

    @GetMapping("/analytics/top-events")
    public ResponseEntity<List<NamedRevenueResponse>> getTopEvents(@RequestParam(defaultValue = "5") int limit) {
        List<NamedRevenueResponse> list = adminService.getRevenueByEvent();
        return ResponseEntity.ok(list.stream().limit(limit).toList());
    }

    @GetMapping("/analytics/top-venues")
    public ResponseEntity<List<NamedRevenueResponse>> getTopVenues(@RequestParam(defaultValue = "5") int limit) {
        List<NamedRevenueResponse> list = adminService.getRevenueByVenue();
        return ResponseEntity.ok(list.stream().limit(limit).toList());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}
