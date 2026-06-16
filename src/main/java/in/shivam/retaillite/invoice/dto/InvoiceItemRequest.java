package in.shivam.retaillite.invoice.dto;

public record InvoiceItemRequest(
        String productId,
        Integer quantity
) {
}
