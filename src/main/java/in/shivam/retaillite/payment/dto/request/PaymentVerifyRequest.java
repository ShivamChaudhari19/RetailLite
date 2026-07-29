package in.shivam.retaillite.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentVerifyRequest(
        @NotBlank
        String gatewayOrderId,
        @NotBlank
        String gatewayPaymentId,
        @NotBlank
        String signature
) {
}
