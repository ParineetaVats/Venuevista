package com.venuevista.repository;

import com.venuevista.entity.HoldStatus;
import com.venuevista.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    List<SeatHold> findByEventIdAndActiveKey(Long eventId, Integer activeKey);

    List<SeatHold> findByEventIdAndUserIdAndActiveKey(Long eventId, Long userId, Integer activeKey);

    List<SeatHold> findByHoldGroup(String holdGroup);

    List<SeatHold> findByStatusAndExpiresAtBefore(HoldStatus status, Instant now);

    @Query("SELECT sh.seat.id FROM SeatHold sh WHERE sh.event.id = :eventId AND sh.activeKey = 1 AND sh.expiresAt > :now")
    Set<Long> findActiveUnexpiredHeldSeatIdsByEventId(@Param("eventId") Long eventId, @Param("now") Instant now);

    @Query("SELECT sh.seat.id FROM SeatHold sh WHERE sh.event.id = :eventId AND sh.user.id = :userId AND sh.activeKey = 1 AND sh.expiresAt > :now")
    Set<Long> findActiveHeldSeatIdsByEventAndUser(@Param("eventId") Long eventId, @Param("userId") Long userId, @Param("now") Instant now);

    @Query("SELECT COUNT(DISTINCT sh.holdGroup) FROM SeatHold sh")
    Long countTotalHoldGroups();

    @Query("SELECT COUNT(DISTINCT sh.holdGroup) FROM SeatHold sh WHERE sh.status = 'CONVERTED'")
    Long countConvertedHoldGroups();

    @Query("SELECT COUNT(sh) > 0 FROM SeatHold sh WHERE sh.seat.id = :seatId AND sh.activeKey = 1 AND sh.expiresAt > CURRENT_TIMESTAMP AND sh.event.eventDate >= CURRENT_DATE")
    boolean existsActiveFutureHoldForSeat(@Param("seatId") Long seatId);
}
