package com.venuevista.repository;

import com.venuevista.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    @Query("SELECT v FROM Venue v WHERE " +
           "(:city IS NULL OR LOWER(v.city) = LOWER(:city)) AND " +
           "(:type IS NULL OR LOWER(v.venueType) = LOWER(:type)) AND " +
           "(:q IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(v.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Venue> filterVenues(@Param("q") String q, @Param("type") String type, @Param("city") String city);
}
