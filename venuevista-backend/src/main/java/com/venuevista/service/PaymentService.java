package com.venuevista.service;

import com.venuevista.dto.PaymentDetailsRequest;
import com.venuevista.entity.Payment;
import com.venuevista.entity.PaymentMethod;
import com.venuevista.entity.PaymentStatus;
import com.venuevista.exception.BookingException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PaymentService {

    private static final Set<String> ALLOWED_BANKS = Set.of("HDFC Bank", "SBI", "ICICI Bank", "Axis Bank");
    private static final Pattern UPI_PATTERN = Pattern.compile("^[\\w.-]+@\\w+$");
    private static final Pattern CARD_LAST4_PATTERN = Pattern.compile("^\\d{4}$");
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public record PaymentResult(PaymentStatus status, String transactionId, String maskedReference) {}

    public PaymentResult processPayment(PaymentMethod method, BigDecimal amount, PaymentDetailsRequest details) {
        String maskedRef = generateMaskedReference(method, details);
        PaymentStatus status = PaymentStatus.PAID;

        if (method == PaymentMethod.UPI && details != null && details.upiId() != null) {
            String upi = details.upiId().trim();
            if (!UPI_PATTERN.matcher(upi).matches()) {
                throw new BookingException("VALIDATION_ERROR", "Invalid UPI ID format");
            }
            if (upi.toLowerCase().endsWith("@fail")) {
                status = PaymentStatus.FAILED;
            }
        } else if (method == PaymentMethod.BANK && details != null && details.bank() != null) {
            if (!ALLOWED_BANKS.contains(details.bank().trim())) {
                throw new BookingException("VALIDATION_ERROR", "Selected bank is not supported for net banking");
            }
        } else if (method == PaymentMethod.CARD && details != null && details.cardLast4() != null) {
            if (!CARD_LAST4_PATTERN.matcher(details.cardLast4().trim()).matches()) {
                throw new BookingException("VALIDATION_ERROR", "Invalid card last 4 digits");
            }
        }

        String transactionId = generateTransactionId();
        return new PaymentResult(status, transactionId, maskedRef);
    }

    private String generateMaskedReference(PaymentMethod method, PaymentDetailsRequest details) {
        return switch (method) {
            case UPI -> {
                if (details != null && details.upiId() != null && details.upiId().contains("@")) {
                    String upi = details.upiId().trim();
                    String[] parts = upi.split("@", 2);
                    String user = parts[0];
                    String maskedUser = user.length() > 2 ? user.substring(0, 2) + "***" : user + "***";
                    yield "UPI: " + maskedUser + "@" + parts[1];
                }
                yield "UPI: Payment";
            }
            case CARD -> {
                if (details != null && details.cardLast4() != null && details.cardLast4().length() == 4) {
                    yield "CARD ****" + details.cardLast4().trim();
                }
                yield "CARD ****0000";
            }
            case BANK -> {
                if (details != null && details.bank() != null) {
                    yield "BANK: " + details.bank().trim();
                }
                yield "BANK: Net Banking";
            }
        };
    }

    private String generateTransactionId() {
        StringBuilder sb = new StringBuilder("TXN-VV-");
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
