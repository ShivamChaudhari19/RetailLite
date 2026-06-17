package in.shivam.retaillite.invoice.dto;

import in.shivam.retaillite.common.enums.PaymentMethod;

import java.util.List;

public record InvoiceRequest(
        String userName,
        String customerName,
        String customerNumber,
        String customerEmail,
        List<InvoiceItemRequest> items
) {
}
