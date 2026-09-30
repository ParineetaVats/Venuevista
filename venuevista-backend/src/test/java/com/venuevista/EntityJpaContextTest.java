package com.venuevista;

import com.venuevista.entity.*;
import com.venuevista.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class EntityJpaContextTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void testEntitiesAndRepositoriesBootOnH2() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("Test@Example.com");
        user.setPasswordHash("hashedpass");
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");

        Venue venue = new Venue();
        venue.setName("Lotus Grand Ballroom");
        venue.setCity("Delhi");
        venue.setVenueType("Wedding");
        venue.setCapacity(600);
        venue.setPrice(new BigDecimal("85000"));
        venue.setColorPrimary("#4b5d78");
        venue.setColorSecondary("#a9b8c9");
        Venue savedVenue = venueRepository.save(venue);

        Seat seat = new Seat(savedVenue, "A", 1, SeatType.VIP);
        Seat savedSeat = seatRepository.save(seat);
        assertThat(savedSeat.getId()).isNotNull();

        Event event = new Event();
        event.setTitle("Sufi Nights Live");
        event.setCategory(EventCategory.CONCERTS);
        event.setEventDate(LocalDate.now().plusDays(30));
        event.setStartTime(LocalTime.of(19, 0));
        event.setVenue(savedVenue);
        event.setBasePrice(new BigDecimal("1499"));
        event.setOrganizer("Raag Live");
        event.setFeatured(true);
        Event savedEvent = eventRepository.save(event);

        assertThat(savedEvent.getId()).isNotNull();
    }
}
