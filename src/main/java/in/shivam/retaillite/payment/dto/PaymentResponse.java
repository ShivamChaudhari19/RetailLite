package in.shivam.retaillite.payment.dto;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.Invoice;
import jakarta.persistence.*;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Builder
public record PaymentResponse(
        String transactionId,
        String invoiceId,
        PaymentMethod paymentmethod,
        PaymentStatus paymentStatus,
        BigDecimal amount,
        Timestamp createdAt,
        String message
) {}
