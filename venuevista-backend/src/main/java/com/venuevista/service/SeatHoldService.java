package com.venuevista.service;

import com.venuevista.dto.HoldResponse;
import com.venuevista.dto.QuoteResponse;
import com.venuevista.dto.SeatDTO;
import com.venuevista.entity.*;
import com.venuevista.exception.ResourceNotFoundException;
import com.venuevista.exception.SeatUnavailableException;
import com.venuevista.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class SeatHoldService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PricingService pricingService;

    @Value("${venuevista.hold.minutes:10}")
    private long holdMinutes;

    public SeatHoldService(
            EventRepository eventRepository,
            SeatRepository seatRepository,
            SeatHoldRepository seatHoldRepository,
            BookingSeatRepository bookingSeatRepository,
            PricingService pricingService
    ) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.pricingService = pricingService;
    }

    @Transactional
    public HoldResponse createHold(Long eventId, List<Long> seatIds, User user) {
        // 1. Lock event row first
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (seatIds == null || seatIds.isEmpty() || seatIds.size() > 8) {
            throw new IllegalArgumentException("Seat selection must contain between 1 and 8 seats.");
        }

        // 2. Clear caller's existing active holds for this event
        List<SeatHold> userHolds = seatHoldRepository.findByEventIdAndUserIdAndActiveKey(eventId, user.getId(), 1);
        for (SeatHold hold : userHolds) {
            hold.setActiveKey(null);
            hold.setStatus(HoldStatus.RELEASED);
        }

        Instant now = Instant.now();
        // 3. Clear expired holds for requested seats to avoid unique constraint issues
        Set<Long> activeBookedSeatIds = bookingSeatRepository.findActiveBookedSeatIdsByEventId(eventId);
        Set<Long> activeHeldSeatIds = seatHoldRepository.findActiveUnexpiredHeldSeatIdsByEventId(eventId, now);

        List<Seat> requestedSeats = new ArrayList<>();
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));

            if (!seat.getVenue().getId().equals(event.getVenue().getId())) {
                throw new IllegalArgumentException("Seat " + seat.getSeatCode() + " does not belong to event venue.");
            }
            if (!seat.isActive()) {
                throw new SeatUnavailableException("Seat " + seat.getSeatCode() + " is unavailable.");
            }
            if (activeBookedSeatIds.contains(seatId) || activeHeldSeatIds.contains(seatId)) {
                throw new SeatUnavailableException();
            }
            requestedSeats.add(seat);
        }

        // 4. Create new holds
        String holdGroup = UUID.randomUUID().toString();
        Instant expiresAt = now.plusSeconds(holdMinutes * 60);

        for (Seat seat : requestedSeats) {
            SeatHold hold = new SeatHold();
            hold.setEvent(event);
            hold.setSeat(seat);
            hold.setUser(user);
            hold.setHoldGroup(holdGroup);
            hold.setStatus(HoldStatus.HELD);
            hold.setExpiresAt(expiresAt);
            hold.setActiveKey(1);
            seatHoldRepository.save(hold);
        }

        QuoteResponse quote = pricingService.calculateQuote(eventId, event.getBasePrice(), requestedSeats);

        List<SeatDTO> seatDTOs = quote.seats().stream()
                .map(s -> new SeatDTO(s.id(), s.code(), s.row(), s.number(), s.type(), s.price(), "HELD", true))
                .toList();

        return new HoldResponse(holdGroup, expiresAt, seatDTOs, quote);
    }

    @Transactional
    public void releaseUserHolds(Long eventId, User user) {
        List<SeatHold> holds = seatHoldRepository.findByEventIdAndUserIdAndActiveKey(eventId, user.getId(), 1);
        for (SeatHold hold : holds) {
            hold.setActiveKey(null);
            hold.setStatus(HoldStatus.RELEASED);
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cleanupExpiredHolds() {
        Instant now = Instant.now();
        List<SeatHold> expired = seatHoldRepository.findByStatusAndExpiresAtBefore(HoldStatus.HELD, now);
        for (SeatHold hold : expired) {
            hold.setStatus(HoldStatus.EXPIRED);
            hold.setActiveKey(null);
        }
    }
}
