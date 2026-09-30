package com.venuevista;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venuevista.dto.*;
import com.venuevista.entity.*;
import com.venuevista.repository.*;
import com.venuevista.security.JwtUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingAndConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private User admin;
    private String tokenA;
    private String tokenB;
    private String tokenAdmin;

    private Event testEvent;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        bookingSeatRepository.deleteAll();
        bookingRepository.deleteAll();
        seatHoldRepository.deleteAll();
        eventRepository.deleteAll();
        seatRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("Pass1234"));
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);
        tokenAdmin = jwtUtils.generateToken(admin.getId(), admin.getEmail(), admin.getRole());

        userA = new User();
        userA.setName("User A");
        userA.setEmail("usera@test.com");
        userA.setPasswordHash(passwordEncoder.encode("Pass1234"));
        userA.setRole(Role.USER);
        userA = userRepository.save(userA);
        tokenA = jwtUtils.generateToken(userA.getId(), userA.getEmail(), userA.getRole());

        userB = new User();
        userB.setName("User B");
        userB.setEmail("userb@test.com");
        userB.setPasswordHash(passwordEncoder.encode("Pass1234"));
        userB.setRole(Role.USER);
        userB = userRepository.save(userB);
        tokenB = jwtUtils.generateToken(userB.getId(), userB.getEmail(), userB.getRole());

        Venue venue = new Venue();
        venue.setName("Grand Hall");
        venue.setCity("Delhi");
        venue.setVenueType("Wedding");
        venue.setCapacity(600);
        venue.setPrice(new BigDecimal("50000"));
        venue = venueRepository.save(venue);

        testSeat = seatRepository.save(new Seat(venue, "A", 1, SeatType.VIP));

        Event event = new Event();
        event.setTitle("Live Sufi Night");
        event.setCategory(EventCategory.CONCERTS);
        event.setEventDate(LocalDate.now().plusDays(10));
        event.setStartTime(LocalTime.of(19, 0));
        event.setVenue(venue);
        event.setBasePrice(new BigDecimal("1000"));
        event.setOrganizer("Raag");
        event.setFeatured(true);
        testEvent = eventRepository.save(event);
    }

    @Test
    void testConcurrencyNThreadsBookingSameSeat() throws Exception {
        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        // Pre-create users and tokens for threads
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            User u = new User();
            u.setName("ThreadUser " + i);
            u.setEmail("thread" + i + "@test.com");
            u.setPasswordHash(passwordEncoder.encode("Pass1234"));
            u.setRole(Role.USER);
            u = userRepository.save(u);
            tokens.add(jwtUtils.generateToken(u.getId(), u.getEmail(), u.getRole()));
        }

        for (int i = 0; i < numThreads; i++) {
            final String token = tokens.get(i);
            final String customerName = "ThreadUser " + i;
            executor.submit(() -> {
                try {
                    latch.await(); // wait for start signal
                    BookingRequest req = new BookingRequest(
                            testEvent.getId(),
                            List.of(testSeat.getId()),
                            customerName,
                            "test@example.com",
                            "9876543210",
                            PaymentMethod.CARD,
                            new PaymentDetailsRequest(null, null, "4242")
                    );

                    MvcResult result = mockMvc.perform(post("/api/bookings")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else if (status == 409) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // release threads simultaneously
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(numThreads - 1);
    }

    @Test
    void testSeatHoldBlocksAnotherUser() throws Exception {
        // User A holds seat
        HoldRequest holdReq = new HoldRequest(List.of(testSeat.getId()));
        mockMvc.perform(post("/api/events/" + testEvent.getId() + "/holds")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdReq)))
                .andExpect(status().isOk());

        // User B tries to book held seat -> 409 SEAT_UNAVAILABLE
        BookingRequest reqB = new BookingRequest(
                testEvent.getId(),
                List.of(testSeat.getId()),
                "User B",
                "userb@test.com",
                "9876543210",
                PaymentMethod.CARD,
                null
        );

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SEAT_UNAVAILABLE"));
    }

    @Test
    void testCancellationFreesSeatForRebooking() throws Exception {
        BookingRequest reqA = new BookingRequest(
                testEvent.getId(),
                List.of(testSeat.getId()),
                "User A",
                "usera@test.com",
                "9876543210",
                PaymentMethod.CARD,
                null
        );

        MvcResult result = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();

        BookingResponse bResp = objectMapper.readValue(result.getResponse().getContentAsString(), BookingResponse.class);

        // Cancel booking
        mockMvc.perform(put("/api/bookings/" + bResp.bookingReference() + "/cancel")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"));

        // Second cancel returns 409
        mockMvc.perform(put("/api/bookings/" + bResp.bookingReference() + "/cancel")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BOOKING_NOT_CANCELLABLE"));

        // Now User B can book the freed seat
        BookingRequest reqB = new BookingRequest(
                testEvent.getId(),
                List.of(testSeat.getId()),
                "User B",
                "userb@test.com",
                "9876543210",
                PaymentMethod.UPI,
                new PaymentDetailsRequest(null, "userb@okbank", null)
        );

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated());
    }

    @Test
    void testUserBCannotReadUserABookingReturns404() throws Exception {
        BookingRequest reqA = new BookingRequest(
                testEvent.getId(),
                List.of(testSeat.getId()),
                "User A",
                "usera@test.com",
                "9876543210",
                PaymentMethod.CARD,
                null
        );

        MvcResult result = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andReturn();

        BookingResponse bResp = objectMapper.readValue(result.getResponse().getContentAsString(), BookingResponse.class);

        // User B tries to read User A's booking reference -> 404
        mockMvc.perform(get("/api/bookings/" + bResp.bookingReference())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // Admin CAN read User A's booking -> 200
        mockMvc.perform(get("/api/bookings/" + bResp.bookingReference())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void testEndToEndFlow() throws Exception {
        // 1. List events (public)
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").exists());

        // 2. Quote
        QuoteRequest quoteReq = new QuoteRequest(testEvent.getId(), List.of(testSeat.getId()));
        mockMvc.perform(post("/api/bookings/quote")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quoteReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(2000)) // Base 1000 x VIP 2.0
                .andExpect(jsonPath("$.total").value(2140)); // 2000 + 40 + 100

        // 3. Book
        BookingRequest bookReq = new BookingRequest(
                testEvent.getId(),
                List.of(testSeat.getId()),
                "User A",
                "usera@test.com",
                "9876543210",
                PaymentMethod.CARD,
                new PaymentDetailsRequest(null, null, "4242")
        );

        MvcResult bookResult = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookReq)))
                .andExpect(status().isCreated())
                .andReturn();

        BookingResponse bResp = objectMapper.readValue(bookResult.getResponse().getContentAsString(), BookingResponse.class);

        // 4. View tickets
        mockMvc.perform(get("/api/tickets/my")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketId").value(bResp.bookingReference() + "-A1"));

        // 5. Admin Analytics Overview
        mockMvc.perform(get("/api/admin/analytics/overview")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").exists());
    }
}
