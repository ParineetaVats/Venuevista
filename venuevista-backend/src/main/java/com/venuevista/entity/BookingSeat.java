package com.venuevista.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_event_seat_active", columnNames = {"event_id", "seat_id", "active_key"})
})
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "active_key")
    private Integer activeKey = 1;

    public BookingSeat() {}

    public BookingSeat(Booking booking, Event event, Seat seat, SeatType seatType, BigDecimal price) {
        this.booking = booking;
        this.event = event;
        this.seat = seat;
        this.seatType = seatType;
        this.price = price;
        this.activeKey = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }

    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getActiveKey() { return activeKey; }
    public void setActiveKey(Integer activeKey) { this.activeKey = activeKey; }
}
