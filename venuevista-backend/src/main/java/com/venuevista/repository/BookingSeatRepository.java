package com.venuevista.repository;

import com.venuevista.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    List<BookingSeat> findByEventIdAndActiveKey(Long eventId, Integer activeKey);

    List<BookingSeat> findByBookingId(Long bookingId);

    @Query("SELECT bs.seat.id FROM BookingSeat bs WHERE bs.event.id = :eventId AND bs.activeKey = 1")
    Set<Long> findActiveBookedSeatIdsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(bs) FROM BookingSeat bs WHERE bs.event.id = :eventId AND bs.activeKey = 1")
    Long countConfirmedSeatsByEventId(@Param("eventId") Long eventId);

    boolean existsBySeatIdAndActiveKey(Long seatId, Integer activeKey);

    @Query("SELECT COUNT(bs) > 0 FROM BookingSeat bs WHERE bs.seat.id = :seatId AND bs.activeKey = 1 AND bs.event.eventDate >= CURRENT_DATE")
    boolean existsActiveFutureBookingForSeat(@Param("seatId") Long seatId);

    long countBySeatId(Long seatId);
}
