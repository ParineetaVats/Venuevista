package com.venuevista.repository;

import com.venuevista.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByVenueIdOrderByRowLabelAscSeatNumberAsc(Long venueId);

    long countByVenueIdAndActiveTrue(Long venueId);

    Optional<Seat> findByVenueIdAndRowLabelAndSeatNumber(Long venueId, String rowLabel, Integer seatNumber);

    Optional<Seat> findByVenueIdAndSeatCode(Long venueId, String seatCode);

    List<Seat> findByVenueIdAndRowLabel(Long venueId, String rowLabel);

    @Query("SELECT DISTINCT s.rowLabel FROM Seat s WHERE s.venue.id = :venueId ORDER BY s.rowLabel ASC")
    List<String> findDistinctRowLabelsByVenueId(Long venueId);

    void deleteByVenueId(Long venueId);
}
