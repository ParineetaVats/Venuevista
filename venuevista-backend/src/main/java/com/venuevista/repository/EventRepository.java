package com.venuevista.repository;

import com.venuevista.entity.Event;
import com.venuevista.entity.EventCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"venue"})
    Optional<Event> findWithVenueById(Long id);

    @EntityGraph(attributePaths = {"venue"})
    List<Event> findByFeaturedTrue();

    @EntityGraph(attributePaths = {"venue"})
    List<Event> findByCategory(EventCategory category);

    @EntityGraph(attributePaths = {"venue"})
    List<Event> findByVenueId(Long venueId);

    @EntityGraph(attributePaths = {"venue"})
    @Query("SELECT e FROM Event e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:city IS NULL OR LOWER(e.venue.city) = LOWER(:city)) AND " +
           "(:from IS NULL OR e.eventDate >= :from) AND " +
           "(:featured IS NULL OR e.featured = :featured) AND " +
           "(:q IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.organizer) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Event> filterEvents(
            @Param("q") String q,
            @Param("category") EventCategory category,
            @Param("city") String city,
            @Param("from") LocalDate from,
            @Param("featured") Boolean featured
    );
}
