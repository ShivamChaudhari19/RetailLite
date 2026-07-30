package in.shivam.retaillite.invoice.mapper;

import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;
import in.shivam.retaillite.invoice.pdf.document.InvoiceItemDocument;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.product.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Component
public class InvoicePdfMapper {
    public InvoiceDocument toDocument(Invoice invoice, Payment payment) {


        List<InvoiceItemDocument> invoiceItemDocuments=
                invoice.getInvoiceItems().stream()
                        .map(this::toItemDocument)
                        .toList();


        return  InvoiceDocument.builder()
                .invoiceId(invoice.getInvoiceId())
                .invoiceIssuer(invoice.getUser().getUserId())
                .createdAt(LocalDateTime.now())
                .customerName(invoice.getCustomerName())
                .customerNumber(invoice.getCustomerNumber())
                .customerEmail(invoice.getCustomerEmail())
                .items(invoiceItemDocuments)
                .subtotal(invoice.getSubTotal()).tax(invoice.getTax())
                .grandTotal(invoice.getGrandTotal())
                // Null-safe checks for Payment fields
                .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
                .paymentStatus(payment != null ? payment.getPaymentStatus() : null)
                .paymentId(payment != null ? payment.getPaymentId() : null)
                .invoiceStatus(invoice.getInvoiceStatus())
                .build();
    }

    private InvoiceItemDocument toItemDocument(InvoiceItem invoiceItem) {

        Product product=invoiceItem.getProduct();

        return InvoiceItemDocument.builder()
                .productId(product.getProductId())
                .productName(product.getName())
                .quantity(invoiceItem.getQuantity())
                .taxRate(product.getTaxRate())
                .taxAmount(
                        (invoiceItem.getLineTotal().multiply(product.getTaxRate()))
                                .divide(BigDecimal.valueOf(100L))
                ).unitPrice(product.getPrice())
                .lineTotal(invoiceItem.getLineTotal())
                .build();
    }
}