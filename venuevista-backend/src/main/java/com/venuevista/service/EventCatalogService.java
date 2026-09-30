package com.venuevista.service;

import com.venuevista.dto.EventResponse;
import com.venuevista.dto.EventSeatsResponse;
import com.venuevista.dto.SeatDTO;
import com.venuevista.dto.VenueResponse;
import com.venuevista.entity.*;
import com.venuevista.exception.ResourceNotFoundException;
import com.venuevista.repository.BookingSeatRepository;
import com.venuevista.repository.EventRepository;
import com.venuevista.repository.SeatHoldRepository;
import com.venuevista.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EventCatalogService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final PricingService pricingService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public EventCatalogService(
            EventRepository eventRepository,
            SeatRepository seatRepository,
            BookingSeatRepository bookingSeatRepository,
            SeatHoldRepository seatHoldRepository,
            PricingService pricingService
    ) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.pricingService = pricingService;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> filterEvents(String q, String category, String city, String from, Boolean featured, String sort) {
        EventCategory catEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                catEnum = EventCategory.fromValue(category);
            } catch (IllegalArgumentException ignored) {}
        }

        java.time.LocalDate fromDate = null;
        if (from != null && !from.isBlank()) {
            try {
                fromDate = java.time.LocalDate.parse(from);
            } catch (Exception ignored) {}
        }

        List<Event> events = eventRepository.filterEvents(q, catEnum, city, fromDate, featured);

        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "price-asc" -> events.sort(Comparator.comparing(Event::getBasePrice));
                case "price-desc" -> events.sort(Comparator.comparing(Event::getBasePrice).reversed());
                case "date" -> events.sort(Comparator.comparing(Event::getEventDate));
                default -> events.sort(Comparator.comparing(Event::getEventDate));
            }
        } else {
            events.sort(Comparator.comparing(Event::getEventDate));
        }

        return events.stream().map(this::toEventResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getFeaturedEvents() {
        return eventRepository.findByFeaturedTrue().stream()
                .map(this::toEventResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByCategory(String category) {
        EventCategory catEnum = EventCategory.fromValue(category);
        return eventRepository.findByCategory(catEnum).stream()
                .map(this::toEventResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(String keyword) {
        return filterEvents(keyword, null, null, null, null, "date");
    }

    public List<String> getCategories() {
        return Arrays.stream(EventCategory.values())
                .map(EventCategory::getDisplayName)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findWithVenueById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return toEventResponse(event);
    }

    @Transactional(readOnly = true)
    public EventSeatsResponse getEventSeats(Long eventId, Long currentUserId) {
        Event event = eventRepository.findWithVenueById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        Venue venue = event.getVenue();
        List<Seat> seats = seatRepository.findByVenueIdOrderByRowLabelAscSeatNumberAsc(venue.getId());

        Set<Long> activeBookedSeatIds = bookingSeatRepository.findActiveBookedSeatIdsByEventId(eventId);
        Instant now = Instant.now();
        Set<Long> activeHeldSeatIds = seatHoldRepository.findActiveUnexpiredHeldSeatIdsByEventId(eventId, now);
        Set<Long> myHeldSeatIds = currentUserId != null ?
                seatHoldRepository.findActiveHeldSeatIdsByEventAndUser(eventId, currentUserId, now) : Collections.emptySet();

        List<SeatDTO> seatDTOs = new ArrayList<>();
        for (Seat seat : seats) {
            String status;
            boolean heldByMe = false;

            if (!seat.isActive()) {
                status = "UNAVAILABLE";
            } else if (activeBookedSeatIds.contains(seat.getId())) {
                status = "BOOKED";
            } else if (activeHeldSeatIds.contains(seat.getId())) {
                status = "HELD";
                if (myHeldSeatIds.contains(seat.getId())) {
                    heldByMe = true;
                }
            } else {
                status = "AVAILABLE";
            }

            java.math.BigDecimal seatPrice = pricingService.calculateSeatPrice(event.getBasePrice(), seat.getSeatType());
            seatDTOs.add(new SeatDTO(
                    seat.getId(),
                    seat.getSeatCode(),
                    seat.getRowLabel(),
                    seat.getSeatNumber(),
                    seat.getSeatType(),
                    seatPrice,
                    status,
                    heldByMe
            ));
        }

        return new EventSeatsResponse(event.getId(), venue.getId(), event.getBasePrice(), seatDTOs);
    }

    public EventResponse toEventResponse(Event event) {
        Venue v = event.getVenue();
        VenueResponse venueResponse = new VenueResponse(
                v.getId(),
                v.getName(),
                v.getCity(),
                v.getVenueType(),
                v.getCapacity(),
                v.getPrice(),
                v.getDescription(),
                v.getColorPrimary(),
                v.getColorSecondary()
        );

        int totalSeats = (int) seatRepository.countByVenueIdAndActiveTrue(v.getId());
        Long bookedCount = bookingSeatRepository.countConfirmedSeatsByEventId(event.getId());
        int leftSeats = Math.max(0, totalSeats - (bookedCount != null ? bookedCount.intValue() : 0));

        String timeStr = event.getStartTime() != null ? event.getStartTime().format(TIME_FORMATTER) : "";

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getCategory(),
                event.getEventDate(),
                timeStr,
                v.getId(),
                venueResponse,
                event.getBasePrice(),
                totalSeats,
                leftSeats,
                event.getFeatured(),
                event.getOrganizer(),
                event.getDescription()
        );
    }
}
