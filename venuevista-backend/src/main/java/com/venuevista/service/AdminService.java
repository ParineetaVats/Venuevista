package com.venuevista.service;

import com.venuevista.dto.*;
import com.venuevista.entity.*;
import com.venuevista.exception.BookingException;
import com.venuevista.exception.ResourceNotFoundException;
import com.venuevista.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class AdminService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final UserRepository userRepository;
    private final EventCatalogService eventCatalogService;
    private final VenueService venueService;
    private final PricingService pricingService;

    @Value("${venuevista.timezone:Asia/Kolkata}")
    private String timezone;

    private static final String[] ROW_LETTERS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};

    public AdminService(
            EventRepository eventRepository,
            VenueRepository venueRepository,
            SeatRepository seatRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            SeatHoldRepository seatHoldRepository,
            UserRepository userRepository,
            EventCatalogService eventCatalogService,
            VenueService venueService,
            PricingService pricingService
    ) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.userRepository = userRepository;
        this.eventCatalogService = eventCatalogService;
        this.venueService = venueService;
        this.pricingService = pricingService;
    }

    // --- EVENTS ---

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.venueId()));

        Event event = new Event();
        event.setTitle(request.title().trim());
        event.setCategory(request.category());
        event.setEventDate(request.date());
        event.setStartTime(parseTime(request.time()));
        event.setVenue(venue);
        event.setBasePrice(request.price());
        event.setFeatured(Boolean.TRUE.equals(request.featured()));
        event.setOrganizer(request.organizer().trim());
        event.setDescription(request.description() != null ? request.description().trim() : "");

        Event saved = eventRepository.save(event);
        return eventCatalogService.toEventResponse(saved);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.venueId()));

        event.setTitle(request.title().trim());
        event.setCategory(request.category());
        event.setEventDate(request.date());
        event.setStartTime(parseTime(request.time()));
        event.setVenue(venue);
        event.setBasePrice(request.price());
        event.setFeatured(Boolean.TRUE.equals(request.featured()));
        event.setOrganizer(request.organizer().trim());
        event.setDescription(request.description() != null ? request.description().trim() : "");

        Event saved = eventRepository.save(event);
        return eventCatalogService.toEventResponse(saved);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        long confirmedBookings = bookingRepository.countByEventIdAndStatus(id, BookingStatus.CONFIRMED);
        if (confirmedBookings > 0) {
            throw new BookingException("EVENT_HAS_BOOKINGS", "Cannot delete event that has confirmed bookings.", 409);
        }
        eventRepository.deleteById(id);
    }

    // --- VENUES ---

    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.name().trim());
        venue.setCity(request.city().trim());
        venue.setVenueType(request.type().trim());
        venue.setCapacity(request.capacity());
        venue.setPrice(request.price());
        venue.setDescription(request.description() != null ? request.description().trim() : "");
        venue.setColorPrimary(request.c1() != null ? request.c1() : "#2f4858");
        venue.setColorSecondary(request.c2() != null ? request.c2() : "#86a8c4");

        Venue saved = venueRepository.save(venue);
        createDefaultLayoutForVenue(saved);
        return venueService.toVenueResponse(saved);
    }

    @Transactional
    public VenueResponse updateVenue(Long id, VenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        venue.setName(request.name().trim());
        venue.setCity(request.city().trim());
        venue.setVenueType(request.type().trim());
        venue.setCapacity(request.capacity());
        venue.setPrice(request.price());
        venue.setDescription(request.description() != null ? request.description().trim() : "");
        if (request.c1() != null) venue.setColorPrimary(request.c1());
        if (request.c2() != null) venue.setColorSecondary(request.c2());

        Venue saved = venueRepository.save(venue);
        return venueService.toVenueResponse(saved);
    }

    @Transactional
    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue not found with id: " + id);
        }
        List<Event> events = eventRepository.findByVenueId(id);
        if (!events.isEmpty()) {
            throw new BookingException("VENUE_HAS_EVENTS", "Cannot delete venue that has hosted events.", 409);
        }
        seatRepository.deleteByVenueId(id);
        venueRepository.deleteById(id);
    }

    // --- SEATING MANAGEMENT ---

    @Transactional(readOnly = true)
    public AdminSeatingResponse getVenueSeating(Long venueId, Long eventId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));

        List<Seat> seats = seatRepository.findByVenueIdOrderByRowLabelAscSeatNumberAsc(venueId);

        Set<Long> activeBookedSeatIds = eventId != null ? bookingSeatRepository.findActiveBookedSeatIdsByEventId(eventId) : Collections.emptySet();
        Set<Long> activeHeldSeatIds = eventId != null ? seatHoldRepository.findActiveUnexpiredHeldSeatIdsByEventId(eventId, Instant.now()) : Collections.emptySet();

        int total = seats.size();
        int unavailable = 0;
        int booked = 0;
        int available = 0;

        List<AdminSeatDTO> seatDTOs = new ArrayList<>();
        for (Seat s : seats) {
            String status;
            if (!s.isActive()) {
                status = "UNAVAILABLE";
                unavailable++;
            } else if (eventId != null && activeBookedSeatIds.contains(s.getId())) {
                status = "BOOKED";
                booked++;
            } else if (eventId != null && activeHeldSeatIds.contains(s.getId())) {
                status = "HELD";
                booked++;
            } else {
                status = "AVAILABLE";
                if (s.isActive()) available++;
            }

            seatDTOs.add(new AdminSeatDTO(
                    s.getId(),
                    s.getSeatCode(),
                    s.getRowLabel(),
                    s.getSeatNumber(),
                    s.getSeatType(),
                    s.isActive(),
                    status
            ));
        }

        Map<String, Object> stats = Map.of(
                "total", total,
                "available", available,
                "booked", booked,
                "unavailable", unavailable
        );

        return new AdminSeatingResponse(venueId, eventId, stats, seatDTOs);
    }

    @Transactional
    public AdminSeatingResponse addSeats(Long venueId, AddSeatRequest request) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));

        List<String> existingRows = seatRepository.findDistinctRowLabelsByVenueId(venueId);

        String rowLabel = request.rowLabel() != null ? request.rowLabel().trim().toUpperCase() : null;
        SeatType seatType = request.seatType();

        if (rowLabel == null || rowLabel.isEmpty() || !existingRows.contains(rowLabel)) {
            // New row logic
            if (existingRows.size() >= 12) {
                throw new BookingException("MAX_ROWS_EXCEEDED", "Maximum 12 rows (A–L) allowed per venue layout.", 400);
            }
            if (rowLabel == null || rowLabel.isEmpty()) {
                rowLabel = ROW_LETTERS[existingRows.size()];
            }
            int count = request.count() != null && request.count() > 0 ? request.count() : 10;
            if (seatType == null) seatType = SeatType.STANDARD;

            for (int i = 1; i <= count; i++) {
                Seat seat = new Seat(venue, rowLabel, i, seatType);
                seatRepository.save(seat);
            }
        } else {
            // Append seats to existing row
            List<Seat> rowSeats = seatRepository.findByVenueIdAndRowLabel(venueId, rowLabel);
            int lastNum = rowSeats.stream().mapToInt(Seat::getSeatNumber).max().orElse(0);
            SeatType rowType = seatType != null ? seatType : (rowSeats.isEmpty() ? SeatType.STANDARD : rowSeats.get(0).getSeatType());

            int count = request.count() != null && request.count() > 0 ? request.count() : 1;
            for (int i = 1; i <= count; i++) {
                Seat seat = new Seat(venue, rowLabel, lastNum + i, rowType);
                seatRepository.save(seat);
            }
        }

        return getVenueSeating(venueId, null);
    }

    @Transactional
    public AdminSeatDTO updateSeat(Long seatId, UpdateSeatRequest request) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));

        validateSeatNotInActiveFutureUse(seatId);

        if (request.seatType() != null) {
            seat.setSeatType(request.seatType());
        }
        if (request.active() != null) {
            seat.setActive(request.active());
        }

        Seat saved = seatRepository.save(seat);
        return new AdminSeatDTO(saved.getId(), saved.getSeatCode(), saved.getRowLabel(), saved.getSeatNumber(), saved.getSeatType(), saved.isActive(), saved.isActive() ? "AVAILABLE" : "UNAVAILABLE");
    }

    @Transactional
    public void deleteSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));

        validateSeatNotInActiveFutureUse(seatId);

        long bookingCount = bookingSeatRepository.countBySeatId(seatId);
        if (bookingCount > 0) {
            // Soft delete if referenced historically
            seat.setActive(false);
            seatRepository.save(seat);
        } else {
            seatRepository.delete(seat);
        }
    }

    @Transactional
    public AdminSeatingResponse applyRowType(Long venueId, String rowLabel, SeatType seatType) {
        List<Seat> seats = seatRepository.findByVenueIdAndRowLabel(venueId, rowLabel.toUpperCase());
        if (seats.isEmpty()) {
            throw new ResourceNotFoundException("Row " + rowLabel + " not found in venue.");
        }

        for (Seat seat : seats) {
            validateSeatNotInActiveFutureUse(seat.getId());
            seat.setSeatType(seatType);
        }
        seatRepository.saveAll(seats);

        return getVenueSeating(venueId, null);
    }

    @Transactional
    public AdminSeatingResponse resetSeatingLayout(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));

        List<Seat> allSeats = seatRepository.findByVenueIdOrderByRowLabelAscSeatNumberAsc(venueId);
        List<String> skipped = new ArrayList<>();

        // Default spec: A,B VIP(8); C,D Premium(10); E,F Standard(12)
        Map<String, SeatType> defaultTypes = Map.of(
                "A", SeatType.VIP, "B", SeatType.VIP,
                "C", SeatType.PREMIUM, "D", SeatType.PREMIUM,
                "E", SeatType.STANDARD, "F", SeatType.STANDARD
        );
        Map<String, Integer> defaultCounts = Map.of(
                "A", 8, "B", 8,
                "C", 10, "D", 10,
                "E", 12, "F", 12
        );

        for (Seat s : allSeats) {
            boolean inUse = bookingSeatRepository.existsActiveFutureBookingForSeat(s.getId()) || seatHoldRepository.existsActiveFutureHoldForSeat(s.getId());
            if (inUse) {
                skipped.add(s.getSeatCode());
                continue;
            }

            String row = s.getRowLabel();
            int num = s.getSeatNumber();

            if (defaultTypes.containsKey(row) && num <= defaultCounts.get(row)) {
                s.setSeatType(defaultTypes.get(row));
                s.setActive(true);
                seatRepository.save(s);
            } else {
                long historical = bookingSeatRepository.countBySeatId(s.getId());
                if (historical > 0) {
                    s.setActive(false);
                    seatRepository.save(s);
                } else {
                    seatRepository.delete(s);
                }
            }
        }

        // Re-create missing default seats if deleted
        for (String row : new String[]{"A", "B", "C", "D", "E", "F"}) {
            int count = defaultCounts.get(row);
            SeatType type = defaultTypes.get(row);
            for (int i = 1; i <= count; i++) {
                if (seatRepository.findByVenueIdAndRowLabelAndSeatNumber(venueId, row, i).isEmpty()) {
                    seatRepository.save(new Seat(venue, row, i, type));
                }
            }
        }

        AdminSeatingResponse response = getVenueSeating(venueId, null);
        return new AdminSeatingResponse(venueId, null, response.stats(), response.seats(), skipped);
    }

    private void validateSeatNotInActiveFutureUse(Long seatId) {
        boolean booked = bookingSeatRepository.existsActiveFutureBookingForSeat(seatId);
        boolean held = seatHoldRepository.existsActiveFutureHoldForSeat(seatId);
        if (booked || held) {
            throw new BookingException("SEAT_IN_USE", "Seat is currently booked or held for a future event and cannot be modified.", 409);
        }
    }

    private void createDefaultLayoutForVenue(Venue venue) {
        String[][] spec = {
                {"A", "VIP", "8"}, {"B", "VIP", "8"},
                {"C", "PREMIUM", "10"}, {"D", "PREMIUM", "10"},
                {"E", "STANDARD", "12"}, {"F", "STANDARD", "12"}
        };
        for (String[] rowSpec : spec) {
            String row = rowSpec[0];
            SeatType type = SeatType.valueOf(rowSpec[1]);
            int count = Integer.parseInt(rowSpec[2]);
            for (int i = 1; i <= count; i++) {
                seatRepository.save(new Seat(venue, row, i, type));
            }
        }
    }

    // --- SALES & ANALYTICS ---

    @Transactional(readOnly = true)
    public SalesSummaryResponse getSalesSummary() {
        ZoneId zone = ZoneId.of(timezone);
        Instant todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant weekStart = LocalDate.now(zone).minusDays(7).atStartOfDay(zone).toInstant();
        Instant monthStart = LocalDate.now(zone).minusDays(30).atStartOfDay(zone).toInstant();

        Long totalTickets = bookingRepository.countConfirmedSeatsTotal();
        Long todayTickets = bookingRepository.countConfirmedSeatsSince(todayStart);
        Long weekTickets = bookingRepository.countConfirmedSeatsSince(weekStart);
        Long monthTickets = bookingRepository.countConfirmedSeatsSince(monthStart);

        Double totalRev = bookingRepository.sumTotalSubtotalRevenue();
        Double todayRev = bookingRepository.sumSubtotalRevenueSince(todayStart);
        Double weekRev = bookingRepository.sumSubtotalRevenueSince(weekStart);
        Double monthRev = bookingRepository.sumSubtotalRevenueSince(monthStart);
        Double gross = bookingRepository.sumGrossCollected();

        return new SalesSummaryResponse(
                totalTickets != null ? totalTickets : 0,
                todayTickets != null ? todayTickets : 0,
                weekTickets != null ? weekTickets : 0,
                monthTickets != null ? monthTickets : 0,
                toBD(totalRev),
                toBD(todayRev),
                toBD(weekRev),
                toBD(monthRev),
                toBD(gross)
        );
    }

    @Transactional(readOnly = true)
    public List<EventSalesResponse> getSalesList(Long eventId, Long venueId, BookingStatus status, LocalDate from, LocalDate to) {
        List<Event> events = eventRepository.findAll();
        List<EventSalesResponse> sales = new ArrayList<>();

        for (Event e : events) {
            if (eventId != null && !e.getId().equals(eventId)) continue;
            if (venueId != null && !e.getVenue().getId().equals(venueId)) continue;
            if (from != null && e.getEventDate().isBefore(from)) continue;
            if (to != null && e.getEventDate().isAfter(to)) continue;

            int totalSeats = (int) seatRepository.countByVenueIdAndActiveTrue(e.getVenue().getId());
            Long confirmedSeats = bookingSeatRepository.countConfirmedSeatsByEventId(e.getId());
            int sold = confirmedSeats != null ? confirmedSeats.intValue() : 0;
            int left = Math.max(0, totalSeats - sold);

            Double rev = bookingRepository.sumSubtotalRevenueSince(Instant.EPOCH); // Filter per event
            // Calculate revenue specifically for this event
            List<Object[]> eventRevs = bookingRepository.findRevenueByEvent();
            BigDecimal eventRev = BigDecimal.ZERO;
            for (Object[] row : eventRevs) {
                if (((Long) row[0]).equals(e.getId())) {
                    eventRev = toBD((Double) row[2]);
                    break;
                }
            }

            String salesStatus = left == 0 ? "Sold Out" : (double) left / totalSeats < 0.3 ? "Selling Fast" : "On Sale";

            sales.add(new EventSalesResponse(
                    e.getId(),
                    e.getTitle(),
                    e.getVenue().getId(),
                    e.getVenue().getName(),
                    e.getEventDate(),
                    totalSeats,
                    sold,
                    left,
                    eventRev,
                    salesStatus
            ));
        }

        return sales;
    }

    @Transactional(readOnly = true)
    public AnalyticsOverviewResponse getAnalyticsOverview() {
        Double totalRev = bookingRepository.sumTotalSubtotalRevenue();
        BigDecimal rev = toBD(totalRev);
        Long ticketsSold = bookingRepository.countConfirmedSeatsTotal();
        long tSold = ticketsSold != null ? ticketsSold : 0;

        BigDecimal avgPrice = tSold > 0 ? rev.divide(BigDecimal.valueOf(tSold), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Long totalHolds = seatHoldRepository.countTotalHoldGroups();
        Long convertedHolds = seatHoldRepository.countConvertedHoldGroups();
        double conversionPct = (totalHolds != null && totalHolds > 0 && convertedHolds != null) ?
                ((double) convertedHolds / totalHolds) * 100.0 : 0.0;

        List<Event> events = eventRepository.findAll();
        long totalActiveCapacity = 0;
        for (Event e : events) {
            totalActiveCapacity += seatRepository.countByVenueIdAndActiveTrue(e.getVenue().getId());
        }

        double occupancyPct = totalActiveCapacity > 0 ? ((double) tSold / totalActiveCapacity) * 100.0 : 0.0;

        Long totalBookings = bookingRepository.countConfirmedBookings();

        return new AnalyticsOverviewResponse(
                rev,
                tSold,
                avgPrice,
                round2(conversionPct),
                round2(occupancyPct),
                totalBookings != null ? totalBookings : 0
        );
    }

    @Transactional(readOnly = true)
    public List<TimeSeriesResponse> getTimeSeriesRevenue(LocalDate from, LocalDate to) {
        ZoneId zone = ZoneId.of(timezone);
        int currentYear = LocalDate.now(zone).getYear();
        LocalDate start = from != null ? from : LocalDate.of(currentYear, 1, 1);
        LocalDate end = to != null ? to : LocalDate.now(zone);

        Instant since = start.atStartOfDay(zone).toInstant();
        List<Object[]> rows = bookingRepository.findConfirmedBookingTimeSeriesData(since);

        // Bucket by YYYY-MM
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        LocalDate curr = start.withDayOfMonth(1);
        while (!curr.isAfter(end.withDayOfMonth(1))) {
            String key = String.format("%04d-%02d", curr.getYear(), curr.getMonthValue());
            monthlyRevenue.put(key, BigDecimal.ZERO);
            curr = curr.plusMonths(1);
        }

        for (Object[] row : rows) {
            Instant createdAt = (Instant) row[0];
            BigDecimal subtotal = (BigDecimal) row[1];
            LocalDate d = createdAt.atZone(zone).toLocalDate();
            String key = String.format("%04d-%02d", d.getYear(), d.getMonthValue());
            if (monthlyRevenue.containsKey(key)) {
                monthlyRevenue.put(key, monthlyRevenue.get(key).add(subtotal));
            }
        }

        List<TimeSeriesResponse> res = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : monthlyRevenue.entrySet()) {
            int monthVal = Integer.parseInt(entry.getKey().substring(5));
            String monthLabel = Month.of(monthVal).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            res.add(new TimeSeriesResponse(entry.getKey(), monthLabel, entry.getValue()));
        }
        return res;
    }

    @Transactional(readOnly = true)
    public List<TimeSeriesResponse> getTimeSeriesTickets(LocalDate from, LocalDate to) {
        ZoneId zone = ZoneId.of(timezone);
        int currentYear = LocalDate.now(zone).getYear();
        LocalDate start = from != null ? from : LocalDate.of(currentYear, 1, 1);
        LocalDate end = to != null ? to : LocalDate.now(zone);

        Instant since = start.atStartOfDay(zone).toInstant();
        List<Object[]> rows = bookingRepository.findConfirmedBookingTimeSeriesData(since);

        Map<String, Long> monthlyTickets = new LinkedHashMap<>();
        LocalDate curr = start.withDayOfMonth(1);
        while (!curr.isAfter(end.withDayOfMonth(1))) {
            String key = String.format("%04d-%02d", curr.getYear(), curr.getMonthValue());
            monthlyTickets.put(key, 0L);
            curr = curr.plusMonths(1);
        }

        for (Object[] row : rows) {
            Instant createdAt = (Instant) row[0];
            Integer qty = (Integer) row[2];
            LocalDate d = createdAt.atZone(zone).toLocalDate();
            String key = String.format("%04d-%02d", d.getYear(), d.getMonthValue());
            if (monthlyTickets.containsKey(key)) {
                monthlyTickets.put(key, monthlyTickets.get(key) + qty);
            }
        }

        List<TimeSeriesResponse> res = new ArrayList<>();
        for (Map.Entry<String, Long> entry : monthlyTickets.entrySet()) {
            int monthVal = Integer.parseInt(entry.getKey().substring(5));
            String monthLabel = Month.of(monthVal).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            res.add(new TimeSeriesResponse(entry.getKey(), monthLabel, entry.getValue()));
        }
        return res;
    }

    @Transactional(readOnly = true)
    public List<NamedRevenueResponse> getRevenueByEvent() {
        return bookingRepository.findRevenueByEvent().stream()
                .map(r -> new NamedRevenueResponse((Long) r[0], (String) r[1], toBD((Double) r[2])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NamedRevenueResponse> getRevenueByVenue() {
        return bookingRepository.findRevenueByVenue().stream()
                .map(r -> new NamedRevenueResponse((Long) r[0], (String) r[1], toBD((Double) r[2])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDistributionResponse> getTicketCategoryDistribution() {
        List<Object[]> rows = bookingRepository.findTicketCategoryDistribution();
        long total = 0;
        for (Object[] r : rows) {
            total += (Long) r[1];
        }

        List<CategoryDistributionResponse> list = new ArrayList<>();
        for (Object[] r : rows) {
            SeatType type = (SeatType) r[0];
            long count = (Long) r[1];
            double pct = total > 0 ? ((double) count / total) * 100.0 : 0.0;
            list.add(new CategoryDistributionResponse(type.getDisplayName(), count, round2(pct)));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<EventOccupancyResponse> getOccupancyByEvent() {
        List<Event> events = eventRepository.findAll();
        List<EventOccupancyResponse> list = new ArrayList<>();
        for (Event e : events) {
            int capacity = (int) seatRepository.countByVenueIdAndActiveTrue(e.getVenue().getId());
            Long confirmed = bookingSeatRepository.countConfirmedSeatsByEventId(e.getId());
            int sold = confirmed != null ? confirmed.intValue() : 0;
            double pct = capacity > 0 ? ((double) sold / capacity) * 100.0 : 0.0;
            list.add(new EventOccupancyResponse(e.getId(), e.getTitle(), capacity, sold, round2(pct)));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long events = eventRepository.count();
        long venues = venueRepository.count();
        long users = userRepository.count();
        Long bookings = bookingRepository.countConfirmedBookings();
        Long tickets = bookingRepository.countConfirmedSeatsTotal();
        Double rev = bookingRepository.sumTotalSubtotalRevenue();

        return new DashboardStatsResponse(
                events,
                venues,
                users,
                bookings != null ? bookings : 0,
                tickets != null ? tickets : 0,
                toBD(rev)
        );
    }

    private BigDecimal toBD(Double d) {
        return d != null ? BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private double round2(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return LocalTime.of(19, 0);
        try {
            return LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
        } catch (Exception e1) {
            try {
                return LocalTime.parse(timeStr.trim());
            } catch (Exception e2) {
                return LocalTime.of(19, 0);
            }
        }
    }
}
