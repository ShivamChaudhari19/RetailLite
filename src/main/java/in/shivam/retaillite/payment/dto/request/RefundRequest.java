package in.shivam.retaillite.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank
        String invoiceId
) {
}
