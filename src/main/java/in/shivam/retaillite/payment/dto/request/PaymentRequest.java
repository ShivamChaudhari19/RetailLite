package in.shivam.retaillite.payment.dto.request;

import in.shivam.retaillite.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotBlank
        String invoiceId,
        @NotNull
        PaymentMethod paymentMethod
) {}
