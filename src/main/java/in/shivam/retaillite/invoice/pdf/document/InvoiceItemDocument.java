package in.shivam.retaillite.invoice.pdf.document;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Immutable, rendering-ready snapshot of a single invoice line item.
 */
@Builder
public record InvoiceItemDocument(
        String productId,
        String productName,
        Integer quantity,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
