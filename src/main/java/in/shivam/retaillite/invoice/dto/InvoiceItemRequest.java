package in.shivam.retaillite.invoice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InvoiceItemRequest(
        @NotNull
        String productId,
        @NotNull
        @Min(1)
        Integer quantity
) {
}
