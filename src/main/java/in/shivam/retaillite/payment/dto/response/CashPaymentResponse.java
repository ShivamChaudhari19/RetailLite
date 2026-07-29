package in.shivam.retaillite.payment.dto.response;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Builder
public record CashPaymentResponse(
        String paymentId,
        String invoiceId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        Timestamp createdAt
)implements PaymentResponse {}
