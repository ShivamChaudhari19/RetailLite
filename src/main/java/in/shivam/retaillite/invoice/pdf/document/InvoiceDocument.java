package in.shivam.retaillite.invoice.pdf.document;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable, rendering-ready snapshot of an invoice.
 * <p>
 * This is deliberately decoupled from {@link in.shivam.retaillite.invoice.entity.Invoice}
 * so that the PDF layer never touches JPA entities, lazy proxies, or persistence
 * concerns - {@link in.shivam.retaillite.invoice.mapper.InvoicePdfMapper} is the
 * only place responsible for producing this object.
 */
@Builder
public record InvoiceDocument(
        String invoiceId,
        String invoiceIssuer,
        LocalDateTime createdAt,
        String customerName,
        String customerNumber,
        String customerEmail,
        List<InvoiceItemDocument> items,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal grandTotal,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String paymentId,
        InvoiceStatus invoiceStatus
) {
}
