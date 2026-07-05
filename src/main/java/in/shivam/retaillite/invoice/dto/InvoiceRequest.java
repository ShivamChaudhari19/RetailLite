package in.shivam.retaillite.invoice.dto;

import in.shivam.retaillite.common.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InvoiceRequest(
        @NotNull
        String customerName,
        @NotNull
        String customerNumber,
        @Email
        String customerEmail,
        @NotEmpty
        List<InvoiceItemRequest> items
) {
}
