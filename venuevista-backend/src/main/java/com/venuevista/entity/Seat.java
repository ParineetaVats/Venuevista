package com.venuevista.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_seat_venue_row_number", columnNames = {"venue_id", "row_label", "seat_number"}),
    @UniqueConstraint(name = "uk_seat_venue_code", columnNames = {"venue_id", "seat_code"})
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "row_label", nullable = false)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Column(nullable = false)
    private boolean active = true;

    public Seat() {}

    public Seat(Venue venue, String rowLabel, Integer seatNumber, SeatType seatType) {
        this.venue = venue;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatCode = rowLabel + seatNumber;
        this.seatType = seatType;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }

    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
