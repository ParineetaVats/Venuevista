package com.venuevista;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venuevista.dto.BookingRequest;
import com.venuevista.dto.PaymentDetailsRequest;
import com.venuevista.dto.UpdateSeatRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityAndAdminApiTest {

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
    private PaymentRepository paymentRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private Event testEvent;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        bookingSeatRepository.deleteAll();
        bookingRepository.deleteAll();
        eventRepository.deleteAll();
        seatRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Regular User");
        user.setEmail("user@test.com");
        user.setPasswordHash(passwordEncoder.encode("Pass1234"));
        user.setRole(Role.USER);
        user = userRepository.save(user);
        userToken = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());

        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("Pass1234"));
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);
        adminToken = jwtUtils.generateToken(admin.getId(), admin.getEmail(), admin.getRole());

        Venue venue = new Venue();
        venue.setName("Hall 1");
        venue.setCity("Delhi");
        venue.setVenueType("Wedding");
        venue.setCapacity(600);
        venue.setPrice(new BigDecimal("50000"));
        venue = venueRepository.save(venue);

        testSeat = seatRepository.save(new Seat(venue, "A", 1, SeatType.VIP));

        Event event = new Event();
        event.setTitle("Test Concert");
        event.setCategory(EventCategory.CONCERTS);
        event.setEventDate(LocalDate.now().plusDays(5));
        event.setStartTime(LocalTime.of(19, 0));
        event.setVenue(venue);
        event.setBasePrice(new BigDecimal("1000"));
        event.setOrganizer("Organizer");
        event.setFeatured(true);
        testEvent = eventRepository.save(event);
    }

    @Test
    void testNoTokenReturns401OnProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/bookings/my"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void testUserTokenReturns403OnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/overview")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void testAdminTokenReturns200OnAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").exists());

        mockMvc.perform(get("/api/admin/sales/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTicketsSold").exists());
    }

    @Test
    void testCorsPreflightAllowedAndDisallowedOrigins() throws Exception {
        // Allowed origin http://localhost:5173 -> 200 OK
        mockMvc.perform(options("/api/events")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        // Disallowed origin http://malicious.com -> Rejected with 403 Forbidden
        mockMvc.perform(options("/api/events")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://malicious.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCannotDeactivateBookedSeatReturns409() throws Exception {
        // First, book the seat
        BookingRequest req = new BookingRequest(
                testEvent.getId(),
                List.of(testSeat.getId()),
                "Regular User",
                "user@test.com",
                "9876543210",
                PaymentMethod.CARD,
                new PaymentDetailsRequest(null, null, "4242")
        );

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Admin attempts to deactivate the booked seat -> 409 SEAT_IN_USE
        UpdateSeatRequest updateReq = new UpdateSeatRequest(null, false);
        mockMvc.perform(put("/api/admin/seats/" + testSeat.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SEAT_IN_USE"));
    }
}
