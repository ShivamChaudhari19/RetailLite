package in.shivam.retaillite.payment.dto.response;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Builder
public record RefundResponse(
        String paymentId,
        String invoiceId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal amount,
        Timestamp createdAt
) {
}
