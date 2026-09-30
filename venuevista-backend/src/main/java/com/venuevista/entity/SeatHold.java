package com.venuevista.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "seat_holds", uniqueConstraints = {
    @UniqueConstraint(name = "uk_hold_event_seat_active", columnNames = {"event_id", "seat_id", "active_key"})
})
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hold_group", nullable = false)
    private String holdGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "active_key")
    private Integer activeKey = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public SeatHold() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getHoldGroup() { return holdGroup; }
    public void setHoldGroup(String holdGroup) { this.holdGroup = holdGroup; }

    public HoldStatus getStatus() { return status; }
    public void setStatus(HoldStatus status) { this.status = status; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Integer getActiveKey() { return activeKey; }
    public void setActiveKey(Integer activeKey) { this.activeKey = activeKey; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
