package in.shivam.retaillite.payment.dto.response;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Builder
public record RazorpayPaymentResponse(
        String paymentId,
        String invoiceId,
        PaymentMethod paymentMethod,
        String gatewayOrderId,
        String gatewayKeyId,
        String currency,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        Timestamp createdAt
)implements PaymentResponse{}
