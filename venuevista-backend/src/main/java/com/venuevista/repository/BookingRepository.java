package com.venuevista.repository;

import com.venuevista.entity.Booking;
import com.venuevista.entity.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"event", "venue", "bookingSeats", "bookingSeats.seat"})
    Optional<Booking> findByBookingReference(String bookingReference);

    @EntityGraph(attributePaths = {"event", "venue", "bookingSeats", "bookingSeats.seat"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"event", "venue", "bookingSeats", "bookingSeats.seat"})
    Optional<Booking> findByBookingReferenceAndUserId(String bookingReference, Long userId);

    long countByEventIdAndStatus(Long eventId, BookingStatus status);

    @Query("SELECT COUNT(bs) FROM BookingSeat bs WHERE bs.event.id = :eventId AND bs.booking.status = 'CONFIRMED'")
    Long countConfirmedSeatsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(bs) FROM BookingSeat bs WHERE bs.booking.status = 'CONFIRMED' AND bs.booking.createdAt >= :since")
    Long countConfirmedSeatsSince(@Param("since") Instant since);

    @Query("SELECT SUM(b.subtotal) FROM Booking b WHERE b.status = 'CONFIRMED' AND b.createdAt >= :since")
    Double sumSubtotalRevenueSince(@Param("since") Instant since);

    @Query("SELECT SUM(b.subtotal) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Double sumTotalSubtotalRevenue();

    @Query("SELECT SUM(b.total) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Double sumGrossCollected();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Long countConfirmedBookings();

    @Query("SELECT COUNT(bs) FROM BookingSeat bs WHERE bs.booking.status = 'CONFIRMED'")
    Long countConfirmedSeatsTotal();

    @Query("SELECT b.createdAt, b.subtotal, b.quantity FROM Booking b WHERE b.status = 'CONFIRMED' AND b.createdAt >= :since ORDER BY b.createdAt ASC")
    List<Object[]> findConfirmedBookingTimeSeriesData(@Param("since") Instant since);

    @Query("SELECT b.event.id, b.event.title, SUM(b.subtotal) FROM Booking b WHERE b.status = 'CONFIRMED' GROUP BY b.event.id, b.event.title ORDER BY SUM(b.subtotal) DESC")
    List<Object[]> findRevenueByEvent();

    @Query("SELECT b.venue.id, b.venue.name, SUM(b.subtotal) FROM Booking b WHERE b.status = 'CONFIRMED' GROUP BY b.venue.id, b.venue.name ORDER BY SUM(b.subtotal) DESC")
    List<Object[]> findRevenueByVenue();

    @Query("SELECT bs.seatType, COUNT(bs) FROM BookingSeat bs WHERE bs.booking.status = 'CONFIRMED' GROUP BY bs.seatType")
    List<Object[]> findTicketCategoryDistribution();

    @Query("SELECT b FROM Booking b WHERE " +
           "(:eventId IS NULL OR b.event.id = :eventId) AND " +
           "(:venueId IS NULL OR b.venue.id = :venueId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:from IS NULL OR b.event.eventDate >= :from) AND " +
           "(:to IS NULL OR b.event.eventDate <= :to)")
    List<Booking> filterBookings(
            @Param("eventId") Long eventId,
            @Param("venueId") Long venueId,
            @Param("status") BookingStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
