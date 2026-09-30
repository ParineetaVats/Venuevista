package com.venuevista.service;

import com.venuevista.dto.EventResponse;
import com.venuevista.dto.VenueReservationRequest;
import com.venuevista.dto.VenueReservationResponse;
import com.venuevista.dto.VenueResponse;
import com.venuevista.entity.BookingStatus;
import com.venuevista.entity.User;
import com.venuevista.entity.Venue;
import com.venuevista.entity.VenueReservation;
import com.venuevista.exception.ResourceNotFoundException;
import com.venuevista.repository.EventRepository;
import com.venuevista.repository.VenueRepository;
import com.venuevista.repository.VenueReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final VenueReservationRepository venueReservationRepository;
    private final EventCatalogService eventCatalogService;

    public VenueService(
            VenueRepository venueRepository,
            EventRepository eventRepository,
            VenueReservationRepository venueReservationRepository,
            EventCatalogService eventCatalogService
    ) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.venueReservationRepository = venueReservationRepository;
        this.eventCatalogService = eventCatalogService;
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> filterVenues(String q, String type, String city) {
        return venueRepository.filterVenues(q, type, city).stream()
                .map(this::toVenueResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VenueResponse getVenueById(Long id) {
        Venue v = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        return toVenueResponse(v);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByVenueId(Long venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new ResourceNotFoundException("Venue not found with id: " + venueId);
        }
        return eventRepository.findByVenueId(venueId).stream()
                .map(eventCatalogService::toEventResponse)
                .toList();
    }

    @Transactional
    public VenueReservationResponse createReservation(Long venueId, VenueReservationRequest request, User user) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));

        VenueReservation reservation = new VenueReservation();
        reservation.setUser(user);
        reservation.setVenue(venue);
        reservation.setReservationDate(request.reservationDate());
        reservation.setAmount(venue.getPrice());
        reservation.setStatus(BookingStatus.CONFIRMED);

        VenueReservation saved = venueReservationRepository.save(reservation);
        return new VenueReservationResponse(
                saved.getId(),
                venue.getId(),
                venue.getName(),
                saved.getReservationDate(),
                saved.getAmount(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<VenueReservationResponse> getUserReservations(Long userId) {
        return venueReservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(r -> new VenueReservationResponse(
                        r.getId(),
                        r.getVenue().getId(),
                        r.getVenue().getName(),
                        r.getReservationDate(),
                        r.getAmount(),
                        r.getStatus(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    public VenueResponse toVenueResponse(Venue v) {
        return new VenueResponse(
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
    }
}
