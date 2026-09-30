package com.venuevista.repository;

import com.venuevista.entity.VenueReservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueReservationRepository extends JpaRepository<VenueReservation, Long> {

    @EntityGraph(attributePaths = {"venue"})
    List<VenueReservation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
