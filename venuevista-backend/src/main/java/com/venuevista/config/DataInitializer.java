package com.venuevista.config;

import com.venuevista.dto.PaymentDetailsRequest;
import com.venuevista.entity.*;
import com.venuevista.repository.*;
import com.venuevista.service.PaymentService;
import com.venuevista.service.PricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final PasswordEncoder passwordEncoder;
    private final PricingService pricingService;
    private final PaymentService paymentService;

    @Value("${venuevista.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${venuevista.timezone:Asia/Kolkata}")
    private String timezone;

    private static final SecureRandom RANDOM = new SecureRandom();

    public DataInitializer(
            UserRepository userRepository,
            VenueRepository venueRepository,
            SeatRepository seatRepository,
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            PaymentRepository paymentRepository,
            SeatHoldRepository seatHoldRepository,
            PasswordEncoder passwordEncoder,
            PricingService pricingService,
            PaymentService paymentService
    ) {
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.paymentRepository = paymentRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.passwordEncoder = passwordEncoder;
        this.pricingService = pricingService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Data seeding is disabled via venuevista.seed.enabled=false.");
            return;
        }

        if (userRepository.count() > 0 || venueRepository.count() > 0) {
            log.info("Database already contains data; skipping initialization.");
            return;
        }

        log.warn("=========================================================================");
        log.warn("SEEDING DEVELOPMENT / DEMO DATA FOR VENUEVISTA BACKEND");
        log.warn("Default Admin: admin@venuevista.com / Admin@123");
        log.warn("Demo Users: aarav@example.com, neha@example.com, rohan@example.com / Test@1234");
        log.warn("WARNING: CHANGE OR REMOVE THESE DEMO CREDENTIALS BEFORE PRODUCTION DEPLOYMENT!");
        log.warn("=========================================================================");

        // 1. Seed Users
        User admin = createUser("VenueVista Admin", "admin@venuevista.com", "9876543210", "Admin@123", Role.ADMIN, "Delhi", "Events", 5, new BigDecimal("100000"));
        User aarav = createUser("Aarav Sharma", "aarav@example.com", "9812345678", "Test@1234", Role.USER, "Delhi", "Concerts", 2, new BigDecimal("5000"));
        User neha = createUser("Neha Verma", "neha@example.com", "9823456789", "Test@1234", Role.USER, "Gurugram", "Theatre", 4, new BigDecimal("8000"));
        User rohan = createUser("Rohan Mehta", "rohan@example.com", "9834567890", "Test@1234", Role.USER, "Noida", "Sports", 2, new BigDecimal("3000"));
        List<User> testUsers = List.of(aarav, neha, rohan);

        // 2. Seed 8 Venues & Seats
        List<Venue> venues = seedVenues();
        Map<Long, List<Seat>> venueSeatsMap = new HashMap<>();
        for (Venue v : venues) {
            venueSeatsMap.put(v.getId(), seedDefault60SeatsForVenue(v));
        }

        // 3. Seed 8 Events
        List<Event> events = seedEvents(venues);

        // 4. Seed Historical Bookings & Analytics Data
        seedHistory(events, venueSeatsMap, testUsers);

        log.info("Data seeding completed successfully!");
    }

    private User createUser(String name, String email, String phone, String password, Role role, String city, String interest, Integer guests, BigDecimal budget) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        u.setCity(city);
        u.setInterest(interest);
        u.setGuests(guests);
        u.setBudget(budget);
        return userRepository.save(u);
    }

    private List<Venue> seedVenues() {
        Object[][] data = {
                {"Lotus Grand Ballroom", "Delhi", "Wedding", 600, "85000", "An elegant ballroom featuring high ceilings and modern crystal chandeliers.", "#4b5d78", "#a9b8c9"},
                {"The Terrace Loft", "Gurugram", "Corporate", 120, "32000", "A chic industrial loft with scenic skyline views for intimate gatherings.", "#2f4858", "#6f8f9b"},
                {"Yamuna Lawn Resort", "Noida", "Wedding", 900, "120000", "Expansive lush green lawns perfect for open-air concerts and grand celebrations.", "#4f6b58", "#a5bfa0"},
                {"Studio Nine", "Delhi", "Party", 80, "18000", "A cozy boutique event space suited for workshops and private parties.", "#5b4d6b", "#a695b8"},
                {"Summit Conference Hall", "Ghaziabad", "Corporate", 250, "45000", "State-of-the-art conference hall equipped with advanced AV tech.", "#3b4a5a", "#8a9bab"},
                {"Saffron Banquet", "Ghaziabad", "Party", 300, "52000", "A vibrant banquet venue offering customizable layouts for festive gatherings.", "#7c6548", "#d1b98f"},
                {"Heritage Haveli Courtyard", "Delhi", "Wedding", 400, "95000", "A rich heritage courtyard blending historic charm with modern amenities.", "#6b4f5a", "#c4a3ab"},
                {"Skyline Rooftop Lounge", "Gurugram", "Party", 150, "40000", "Panoramic rooftop venue designed for evening comedy shows and networking.", "#34506a", "#86a8c4"}
        };

        List<Venue> list = new ArrayList<>();
        for (Object[] d : data) {
            Venue v = new Venue();
            v.setName((String) d[0]);
            v.setCity((String) d[1]);
            v.setVenueType((String) d[2]);
            v.setCapacity((Integer) d[3]);
            v.setPrice(new BigDecimal((String) d[4]));
            v.setDescription((String) d[5]);
            v.setColorPrimary((String) d[6]);
            v.setColorSecondary((String) d[7]);
            list.add(venueRepository.save(v));
        }
        return list;
    }

    private List<Seat> seedDefault60SeatsForVenue(Venue venue) {
        String[][] spec = {
                {"A", "VIP", "8"}, {"B", "VIP", "8"},
                {"C", "PREMIUM", "10"}, {"D", "PREMIUM", "10"},
                {"E", "STANDARD", "12"}, {"F", "STANDARD", "12"}
        };
        List<Seat> seats = new ArrayList<>();
        for (String[] rowSpec : spec) {
            String row = rowSpec[0];
            SeatType type = SeatType.valueOf(rowSpec[1]);
            int count = Integer.parseInt(rowSpec[2]);
            for (int i = 1; i <= count; i++) {
                seats.add(seatRepository.save(new Seat(venue, row, i, type)));
            }
        }
        return seats;
    }

    private List<Event> seedEvents(List<Venue> venues) {
        // Map 1-based venue id to index in venues list
        Object[][] data = {
                {"Sufi Nights Live", EventCategory.CONCERTS, "2026-11-14", "19:00", 2, "1499", true, "Raag Live Entertainment", "An open-air evening of Sufi and qawwali classics performed by an ensemble of award-winning artists under the stars."},
                {"Capital Cricket League Finale", EventCategory.SPORTS, "2026-12-06", "18:30", 2, "799", true, "Capital Sports Board", "The season-ending showdown between the two top-ranked city teams, with live commentary and fan zones."},
                {"Product Design Summit", EventCategory.CONFERENCES, "2026-12-05", "09:30", 4, "2499", true, "DesignForward India", "A full-day summit with keynotes, panels and portfolio reviews from product leaders across India."},
                {"Pottery & Ceramics Workshop", EventCategory.WORKSHOPS, "2026-10-24", "11:00", 3, "899", false, "Clay Studio Collective", "A hands-on beginner workshop: throw your first pot on the wheel and take your creation home."},
                {"Laugh Riot: Stand-up Special", EventCategory.COMEDY, "2026-10-30", "20:00", 7, "599", true, "Punchline Productions", "Four of the country's sharpest stand-up comics take the rooftop stage for one unforgettable night."},
                {"The Last Ember", EventCategory.THEATRE, "2026-11-21", "19:30", 6, "999", false, "Rangmanch Theatre Group", "A critically acclaimed period drama about a family holding on to its legacy, staged in a heritage courtyard."},
                {"Diwali Lights Festival", EventCategory.FESTIVALS, "2026-11-08", "17:00", 5, "499", false, "Utsav Events", "Food stalls, folk performances, craft markets and a spectacular lights show to celebrate the festival of lights."},
                {"Startup Founders Summit", EventCategory.CORPORATE_EVENTS, "2026-10-12", "10:00", 1, "1999", false, "FoundersCircle", "Meet investors, mentors and fellow founders through curated talks and structured networking sessions."}
        };

        List<Event> list = new ArrayList<>();
        for (Object[] d : data) {
            Event e = new Event();
            e.setTitle((String) d[0]);
            e.setCategory((EventCategory) d[1]);
            e.setEventDate(LocalDate.parse((String) d[2]));
            e.setStartTime(LocalTime.parse((String) d[3]));
            e.setVenue(venues.get((Integer) d[4]));
            e.setBasePrice(new BigDecimal((String) d[5]));
            e.setFeatured((Boolean) d[6]);
            e.setOrganizer((String) d[7]);
            e.setDescription((String) d[8]);
            list.add(eventRepository.save(e));
        }
        return list;
    }

    private void seedHistory(List<Event> events, Map<Long, List<Seat>> venueSeatsMap, List<User> testUsers) {
        ZoneId zone = ZoneId.of(timezone);
        Instant now = Instant.now();

        // Target sold ratios matching frontend data (900->620 left = 280 sold out of 900 -> 31% ratio -> approx 19 seats out of 60)
        double[] soldRatios = {0.31, 0.40, 0.44, 0.20, 0.36, 0.47, 0.13, 0.42};

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            List<Seat> seats = venueSeatsMap.get(event.getVenue().getId());
            int targetSeatCount = (int) Math.round(soldRatios[i] * seats.size());

            int seatIndex = 0;
            while (seatIndex < targetSeatCount && seatIndex < seats.size()) {
                int bookingQty = Math.min(1 + RANDOM.nextInt(3), targetSeatCount - seatIndex);
                List<Seat> bookingSeatsList = new ArrayList<>();
                for (int q = 0; q < bookingQty && seatIndex < seats.size(); q++) {
                    bookingSeatsList.add(seats.get(seatIndex++));
                }

                User buyer = testUsers.get(RANDOM.nextInt(testUsers.size()));
                PaymentMethod method = PaymentMethod.values()[RANDOM.nextInt(PaymentMethod.values().length)];

                // Spread created_at over the last ~9 months (include recent 24h & 7d)
                int daysAgo = RANDOM.nextInt(270);
                if (i == 0 && seatIndex <= 4) daysAgo = 0; // Today
                if (i == 1 && seatIndex <= 4) daysAgo = 3; // Week

                Instant createdAt = now.minus(daysAgo, ChronoUnit.DAYS).minus(RANDOM.nextInt(12), ChronoUnit.HOURS);

                var quote = pricingService.calculateQuote(event.getId(), event.getBasePrice(), bookingSeatsList);

                Booking b = new Booking();
                b.setBookingReference("VV" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
                b.setUser(buyer);
                b.setEvent(event);
                b.setVenue(event.getVenue());
                b.setQuantity(quote.quantity());
                b.setSubtotal(quote.subtotal());
                b.setConvenienceFee(quote.convenienceFee());
                b.setTax(quote.tax());
                b.setTotal(quote.total());
                b.setPaymentMethod(method);
                b.setPaymentStatus(PaymentStatus.PAID);
                b.setStatus(BookingStatus.CONFIRMED);
                b.setCustomerName(buyer.getName());
                b.setCustomerEmail(buyer.getEmail());
                b.setCustomerPhone(buyer.getPhone());
                b.setCreatedAt(createdAt);

                Booking saved = bookingRepository.save(b);

                for (Seat seat : bookingSeatsList) {
                    BigDecimal seatPrice = pricingService.calculateSeatPrice(event.getBasePrice(), seat.getSeatType());
                    BookingSeat bs = new BookingSeat(saved, event, seat, seat.getSeatType(), seatPrice);
                    bookingSeatRepository.save(bs);
                }

                PaymentDetailsRequest details = switch (method) {
                    case UPI -> new PaymentDetailsRequest(null, buyer.getName().toLowerCase().replace(" ", "") + "@okbank", null);
                    case CARD -> new PaymentDetailsRequest(null, null, "4242");
                    case BANK -> new PaymentDetailsRequest("HDFC Bank", null, null);
                };
                PaymentService.PaymentResult pResult = paymentService.processPayment(method, quote.total(), details);

                Payment p = new Payment();
                p.setBooking(saved);
                p.setTransactionId(pResult.transactionId());
                p.setPaymentMethod(method);
                p.setAmount(quote.total());
                p.setStatus(PaymentStatus.PAID);
                p.setMaskedReference(pResult.maskedReference());
                p.setCreatedAt(createdAt);
                paymentRepository.save(p);
            }
        }

        // Add 2 cancelled bookings
        Event sampleEvent = events.get(0);
        Seat cancelSeat = venueSeatsMap.get(sampleEvent.getVenue().getId()).get(58);
        var cancelQuote = pricingService.calculateQuote(sampleEvent.getId(), sampleEvent.getBasePrice(), List.of(cancelSeat));
        User cancelBuyer = testUsers.get(0);

        Booking cb = new Booking();
        cb.setBookingReference("VV" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        cb.setUser(cancelBuyer);
        cb.setEvent(sampleEvent);
        cb.setVenue(sampleEvent.getVenue());
        cb.setQuantity(1);
        cb.setSubtotal(cancelQuote.subtotal());
        cb.setConvenienceFee(cancelQuote.convenienceFee());
        cb.setTax(cancelQuote.tax());
        cb.setTotal(cancelQuote.total());
        cb.setPaymentMethod(PaymentMethod.CARD);
        cb.setPaymentStatus(PaymentStatus.REFUNDED);
        cb.setStatus(BookingStatus.CANCELLED);
        cb.setCustomerName(cancelBuyer.getName());
        cb.setCustomerEmail(cancelBuyer.getEmail());
        cb.setCustomerPhone(cancelBuyer.getPhone());
        cb.setCreatedAt(now.minus(5, ChronoUnit.DAYS));

        Booking savedCb = bookingRepository.save(cb);
        BookingSeat cbs = new BookingSeat(savedCb, sampleEvent, cancelSeat, cancelSeat.getSeatType(), cancelQuote.subtotal());
        cbs.setActiveKey(null); // NULL activeKey for cancelled seat
        bookingSeatRepository.save(cbs);

        // Add 3 CONVERTED and 2 EXPIRED holds for conversion % analytics
        for (int h = 0; h < 3; h++) {
            SeatHold hold = new SeatHold();
            hold.setEvent(events.get(h));
            hold.setSeat(venueSeatsMap.get(events.get(h).getVenue().getId()).get(0));
            hold.setUser(testUsers.get(0));
            hold.setHoldGroup(UUID.randomUUID().toString());
            hold.setStatus(HoldStatus.CONVERTED);
            hold.setExpiresAt(now.minus(1, ChronoUnit.DAYS));
            hold.setActiveKey(null);
            seatHoldRepository.save(hold);
        }
        for (int h = 0; h < 2; h++) {
            SeatHold hold = new SeatHold();
            hold.setEvent(events.get(h));
            hold.setSeat(venueSeatsMap.get(events.get(h).getVenue().getId()).get(1));
            hold.setUser(testUsers.get(1));
            hold.setHoldGroup(UUID.randomUUID().toString());
            hold.setStatus(HoldStatus.EXPIRED);
            hold.setExpiresAt(now.minus(2, ChronoUnit.DAYS));
            hold.setActiveKey(null);
            seatHoldRepository.save(hold);
        }
    }
}
