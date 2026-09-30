package com.venuevista.service;

import com.venuevista.dto.*;
import com.venuevista.entity.*;
import com.venuevista.exception.BookingException;
import com.venuevista.exception.ResourceNotFoundException;
import com.venuevista.exception.SeatUnavailableException;
import com.venuevista.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BookingService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;
    private final PaymentService paymentService;

    @Value("${venuevista.timezone:Asia/Kolkata}")
    private String timezone;

    @Value("${venuevista.booking.max-seats:8}")
    private int maxSeats;

    private static final String REF_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public BookingService(
            EventRepository eventRepository,
            SeatRepository seatRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            SeatHoldRepository seatHoldRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            PricingService pricingService,
            PaymentService paymentService
    ) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.pricingService = pricingService;
        this.paymentService = paymentService;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, User user) {
        // 1. FIRST statement in transaction: PESSIMISTIC_WRITE lock on Event
        Event event = eventRepository.findByIdForUpdate(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.eventId()));

        // 2. Validate seat selection
        List<Long> seatIds = request.seatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            throw new BookingException("TOO_MANY_SEATS", "At least one seat must be selected.", 400);
        }
        Set<Long> uniqueSeatIds = new HashSet<>(seatIds);
        if (uniqueSeatIds.size() != seatIds.size() || seatIds.size() > maxSeats) {
            throw new BookingException("TOO_MANY_SEATS", "Maximum " + maxSeats + " seats allowed per booking.", 400);
        }

        LocalDate today = LocalDate.now(ZoneId.of(timezone));
        if (event.getEventDate().isBefore(today)) {
            throw new BookingException("EVENT_PAST", "Cannot book tickets for past events.", 400);
        }

        Instant now = Instant.now();
        Set<Long> activeBookedSeatIds = bookingSeatRepository.findActiveBookedSeatIdsByEventId(event.getId());
        Set<Long> activeHeldSeatIds = seatHoldRepository.findActiveUnexpiredHeldSeatIdsByEventId(event.getId(), now);
        Set<Long> userHeldSeatIds = seatHoldRepository.findActiveHeldSeatIdsByEventAndUser(event.getId(), user.getId(), now);

        List<Seat> requestedSeats = new ArrayList<>();
        for (Long seatId : uniqueSeatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));

            if (!seat.getVenue().getId().equals(event.getVenue().getId())) {
                throw new BookingException("INVALID_SEAT", "Seat does not belong to event venue.", 400);
            }
            if (!seat.isActive()) {
                throw new SeatUnavailableException("Seat " + seat.getSeatCode() + " is currently inactive.");
            }
            // Check double-booking or hold by another user
            if (activeBookedSeatIds.contains(seatId)) {
                throw new SeatUnavailableException();
            }
            if (activeHeldSeatIds.contains(seatId) && !userHeldSeatIds.contains(seatId)) {
                throw new SeatUnavailableException();
            }
            requestedSeats.add(seat);
        }

        // 3. Server-side Quote calculation
        QuoteResponse quote = pricingService.calculateQuote(event.getId(), event.getBasePrice(), requestedSeats);

        // 4. Process payment
        PaymentService.PaymentResult paymentResult = paymentService.processPayment(
                request.paymentMethod(),
                quote.total(),
                request.paymentDetails()
        );

        if (paymentResult.status() == PaymentStatus.FAILED) {
            throw new BookingException("PAYMENT_FAILED", "Payment processing failed.", 402);
        }

        // 5. Generate Booking Reference
        String bookingRef = generateUniqueBookingReference();

        Booking booking = new Booking();
        booking.setBookingReference(bookingRef);
        booking.setUser(user);
        booking.setEvent(event);
        booking.setVenue(event.getVenue());
        booking.setQuantity(quote.quantity());
        booking.setSubtotal(quote.subtotal());
        booking.setConvenienceFee(quote.convenienceFee());
        booking.setTax(quote.tax());
        booking.setTotal(quote.total());
        booking.setPaymentMethod(request.paymentMethod());
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCustomerName(request.customerName().trim());
        booking.setCustomerEmail(request.customerEmail().trim().toLowerCase());
        booking.setCustomerPhone(request.customerPhone().trim());

        Booking savedBooking = bookingRepository.save(booking);

        // 6. Save Booking Seats with activeKey = 1
        List<BookingSeatDTO> bookingSeatDTOs = new ArrayList<>();
        try {
            for (Seat seat : requestedSeats) {
                BigDecimal price = pricingService.calculateSeatPrice(event.getBasePrice(), seat.getSeatType());
                BookingSeat bookingSeat = new BookingSeat(savedBooking, event, seat, seat.getSeatType(), price);
                bookingSeatRepository.save(bookingSeat);
                bookingSeatDTOs.add(new BookingSeatDTO(
                        seat.getId(),
                        seat.getSeatCode(),
                        seat.getRowLabel(),
                        seat.getSeatNumber(),
                        seat.getSeatType(),
                        price
                ));
            }
            bookingSeatRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new SeatUnavailableException("One or more selected seats were just booked by another user.");
        }

        // 7. Save Payment Entity
        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setTransactionId(paymentResult.transactionId());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setAmount(quote.total());
        payment.setStatus(PaymentStatus.PAID);
        payment.setMaskedReference(paymentResult.maskedReference());
        paymentRepository.save(payment);

        // 8. Convert user's holds for these seats
        List<SeatHold> holds = seatHoldRepository.findByEventIdAndUserIdAndActiveKey(event.getId(), user.getId(), 1);
        for (SeatHold hold : holds) {
            if (uniqueSeatIds.contains(hold.getSeat().getId())) {
                hold.setActiveKey(null);
                hold.setStatus(HoldStatus.CONVERTED);
            }
        }

        return toBookingResponse(savedBooking, paymentResult.transactionId(), bookingSeatDTOs);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookings.stream().map(this::toBookingResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingRef, User user) {
        Booking booking = bookingRepository.findByBookingReference(bookingRef)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingRef));

        if (!user.getRole().equals(Role.ADMIN) && !booking.getUser().getId().equals(user.getId())) {
            // Return 404 to avoid leaking booking existence
            throw new ResourceNotFoundException("Booking not found with reference: " + bookingRef);
        }

        return toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingRef, User user) {
        Booking booking = bookingRepository.findByBookingReference(bookingRef)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingRef));

        if (!user.getRole().equals(Role.ADMIN) && !booking.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Booking not found with reference: " + bookingRef);
        }

        // Lock event row first
        eventRepository.findByIdForUpdate(booking.getEvent().getId());

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("BOOKING_NOT_CANCELLABLE", "Booking is already cancelled or non-cancellable.", 409);
        }

        LocalDate today = LocalDate.now(ZoneId.of(timezone));
        if (booking.getEvent().getEventDate().isBefore(today)) {
            throw new BookingException("BOOKING_NOT_CANCELLABLE", "Cannot cancel bookings for past events.", 409);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.REFUNDED);

        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(booking.getId());
        for (BookingSeat bs : seats) {
            bs.setActiveKey(null);
        }

        Optional<Payment> paymentOpt = paymentRepository.findByBookingId(booking.getId());
        paymentOpt.ifPresent(p -> p.setStatus(PaymentStatus.REFUNDED));

        return toBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getUserTickets(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<TicketResponse> tickets = new ArrayList<>();

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }
            Event event = booking.getEvent();
            String timeStr = formatTime(event.getStartTime());
            String displayStatus = computeDisplayStatus(booking, event.getEventDate());

            for (BookingSeat bs : booking.getBookingSeats()) {
                String ticketId = booking.getBookingReference() + "-" + bs.getSeat().getSeatCode();
                tickets.add(new TicketResponse(
                        ticketId,
                        booking.getBookingReference(),
                        bs.getSeat().getSeatCode(),
                        bs.getSeatType(),
                        bs.getPrice(),
                        event.getId(),
                        event.getTitle(),
                        event.getEventDate(),
                        timeStr,
                        booking.getVenue().getName(),
                        booking.getVenue().getCity(),
                        displayStatus
                ));
            }
        }
        return tickets;
    }

    public QuoteResponse getQuote(QuoteRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.eventId()));

        List<Seat> seats = new ArrayList<>();
        for (Long seatId : request.seatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));
            seats.add(seat);
        }

        return pricingService.calculateQuote(event.getId(), event.getBasePrice(), seats);
    }

    private BookingResponse toBookingResponse(Booking booking) {
        String txId = paymentRepository.findByBookingId(booking.getId())
                .map(Payment::getTransactionId)
                .orElse("TXN-VV-UNKNOWN");

        List<BookingSeatDTO> seatDTOs = booking.getBookingSeats().stream()
                .map(bs -> new BookingSeatDTO(
                        bs.getSeat().getId(),
                        bs.getSeat().getSeatCode(),
                        bs.getSeat().getRowLabel(),
                        bs.getSeat().getSeatNumber(),
                        bs.getSeatType(),
                        bs.getPrice()
                ))
                .toList();

        return toBookingResponse(booking, txId, seatDTOs);
    }

    private BookingResponse toBookingResponse(Booking booking, String transactionId, List<BookingSeatDTO> seatDTOs) {
        Event event = booking.getEvent();
        Venue venue = booking.getVenue();
        String timeStr = formatTime(event.getStartTime());
        String displayStatus = computeDisplayStatus(booking, event.getEventDate());

        return new BookingResponse(
                booking.getBookingReference(),
                booking.getStatus(),
                displayStatus,
                booking.getPaymentStatus(),
                booking.getPaymentMethod(),
                transactionId,
                event.getId(),
                event.getTitle(),
                event.getCategory(),
                event.getEventDate(),
                timeStr,
                venue.getId(),
                venue.getName(),
                venue.getCity(),
                seatDTOs,
                booking.getQuantity(),
                booking.getSubtotal(),
                booking.getConvenienceFee(),
                booking.getTax(),
                booking.getTotal(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getCustomerPhone(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }

    private String computeDisplayStatus(Booking booking, LocalDate eventDate) {
        LocalDate today = LocalDate.now(ZoneId.of(timezone));
        if (booking.getStatus() == BookingStatus.CONFIRMED && eventDate.isBefore(today)) {
            return "COMPLETED";
        }
        return booking.getStatus().name();
    }

    private String formatTime(LocalTime time) {
        if (time == null) return "";
        return time.format(TIME_FORMATTER);
    }

    private String generateUniqueBookingReference() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder("VV");
            for (int i = 0; i < 10; i++) {
                sb.append(REF_ALPHABET.charAt(RANDOM.nextInt(REF_ALPHABET.length())));
            }
            String ref = sb.toString();
            if (bookingRepository.findByBookingReference(ref).isEmpty()) {
                return ref;
            }
        }
        return "VV" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
