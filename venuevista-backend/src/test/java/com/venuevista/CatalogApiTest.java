package com.venuevista;

import com.venuevista.entity.Event;
import com.venuevista.entity.EventCategory;
import com.venuevista.entity.Seat;
import com.venuevista.entity.SeatType;
import com.venuevista.entity.Venue;
import com.venuevista.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CatalogApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    private Event testEvent;
    private Venue testVenue;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        bookingSeatRepository.deleteAll();
        bookingRepository.deleteAll();
        seatHoldRepository.deleteAll();
        eventRepository.deleteAll();
        seatRepository.deleteAll();
        venueRepository.deleteAll();

        testVenue = new Venue();
        testVenue.setName("Royal Palace");
        testVenue.setCity("Delhi");
        testVenue.setVenueType("Wedding");
        testVenue.setCapacity(600);
        testVenue.setPrice(new BigDecimal("85000"));
        testVenue.setColorPrimary("#4b5d78");
        testVenue.setColorSecondary("#a9b8c9");
        testVenue = venueRepository.save(testVenue);

        seatRepository.save(new Seat(testVenue, "A", 1, SeatType.VIP));

        Event event = new Event();
        event.setTitle("Sufi Night Live");
        event.setCategory(EventCategory.CONCERTS);
        event.setEventDate(LocalDate.now().plusDays(15));
        event.setStartTime(LocalTime.of(19, 0));
        event.setVenue(testVenue);
        event.setBasePrice(new BigDecimal("1499"));
        event.setOrganizer("Raag Live");
        event.setFeatured(true);
        testEvent = eventRepository.save(event);
    }

    @Test
    void testEventsListAndDetail() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sufi Night Live"));

        mockMvc.perform(get("/api/events/" + testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.category").value("Concerts"));

        mockMvc.perform(get("/api/events/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void testVenuesListAndDetail() throws Exception {
        mockMvc.perform(get("/api/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Royal Palace"));

        mockMvc.perform(get("/api/venues/" + testVenue.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.c1").value("#4b5d78"))
                .andExpect(jsonPath("$.c2").value("#a9b8c9"));
    }
}
