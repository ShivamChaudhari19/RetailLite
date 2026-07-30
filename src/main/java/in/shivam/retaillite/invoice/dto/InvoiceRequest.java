package in.shivam.retaillite.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InvoiceRequest(
        @NotNull
        String customerName,
        @NotNull
        @Size(min=10,max = 10)
        String customerNumber,
        @Email
        String customerEmail,
        @NotEmpty
        List<@Valid InvoiceItemRequest> items
) {
}
