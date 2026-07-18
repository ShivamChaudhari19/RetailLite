package in.shivam.retaillite.invoice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTest {

    @Test
    @DisplayName("markPaid() shifts status from PENDING to PAID")
    void testMarkPaid() {
        Invoice invoice = Invoice.builder()
                .invoiceStatus(InvoiceStatus.PENDING)
                .build();

        // Act
        invoice.markPaid();

        // Assert
        assertEquals(InvoiceStatus.PAID, invoice.getInvoiceStatus());
    }

    @Test
    @DisplayName("markCanceled() shifts status from PENDING to CANCELED")
    void testMarkCanceled() {
        // Arrange
        Invoice invoice = Invoice.builder()
                .invoiceStatus(InvoiceStatus.PENDING)
                .build();

        // Act
        invoice.markCanceled();

        // Assert
        assertEquals(InvoiceStatus.CANCELED, invoice.getInvoiceStatus());
    }
}