package in.shivam.retaillite.payment.domain.validation;

import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.payment.exception.InvoiceAlreadyPaidException;
import in.shivam.retaillite.payment.exception.InvoiceCanceledException;
import in.shivam.retaillite.payment.exception.InvoiceNotPaidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceValidationTest {

    InvoiceValidation validation=new InvoiceValidation();

    @Test
    void shouldProcessValidateCanPay_WhenInvoiceStatusIsNotPaidAndNotCanceled(){
        Invoice invoice=getInvoice(InvoiceStatus.PENDING);
        assertDoesNotThrow(()->validation.validateCanPay(invoice));
    }

    @Test
    void shouldThrowInvoiceAlreadyPaidException_WhenInvoiceStatusIsPaid(){
        Invoice invoice=getInvoice(InvoiceStatus.PAID);
        InvoiceAlreadyPaidException exception=assertThrows(InvoiceAlreadyPaidException.class,()->validation.validateCanPay(invoice));
        assertEquals("Invoice is already paid....",exception.getMessage());

    }
    @Test
    void shouldThrowInvoiceCanceledException_WhenInvoiceStatusIsCanceled(){
        Invoice invoice=getInvoice(InvoiceStatus.CANCELED);
        InvoiceCanceledException exception=assertThrows(InvoiceCanceledException.class,()->validation.validateCanPay(invoice));
        assertEquals("This Invoice is canceled...",exception.getMessage());

    }

    @Test
    void shouldProcessRefund_WhenInvoiceStatusIsPaid(){
        Invoice invoice=getInvoice(InvoiceStatus.PAID);
        assertDoesNotThrow(()->validation.validateCanRefund(invoice));
    }
    @Test
    void shouldThrowInvoiceNotPaidException_WhenInvoiceStatusPending(){
        Invoice invoice=getInvoice(InvoiceStatus.PENDING);
        InvoiceNotPaidException exception=assertThrows(InvoiceNotPaidException.class,()->validation.validateCanRefund(invoice));
        assertEquals("Invoice:"+invoice.getInvoiceId()+" is not Paid, Refund Cannot Possible ",exception.getMessage());

    }
    @Test
    void shouldThrowInvoiceNotPaidException_WhenInvoiceStatusCanceled(){
        Invoice invoice=getInvoice(InvoiceStatus.CANCELED);
        InvoiceNotPaidException exception=assertThrows(InvoiceNotPaidException.class,()->validation.validateCanRefund(invoice));
        assertEquals("Invoice:"+invoice.getInvoiceId()+" is not Paid, Refund Cannot Possible ",exception.getMessage());
    }

    @Test
    void shouldCompletePayment_WhenInvoiceStatusIsPending(){
        Invoice invoice=getInvoice(InvoiceStatus.PENDING);
        assertDoesNotThrow(()->validation.validateCanCompletePayment(invoice));
    }
    @Test
    void shouldNotCompletePayment_WhenInvoiceIsPaid(){
        Invoice invoice=getInvoice(InvoiceStatus.PAID);
        InvoiceAlreadyPaidException exception=assertThrows(InvoiceAlreadyPaidException.class,()->validation.validateCanCompletePayment(invoice));
        assertEquals("Invoice: "+invoice.getInvoiceId()+" is already paid at: "+invoice.getUpdatedAt(),exception.getMessage());

    }
    @Test
    void shouldNotCompletePayment_WhenInvoiceIsCanceled(){
        Invoice invoice=getInvoice(InvoiceStatus.CANCELED);
        InvoiceCanceledException exception=assertThrows(InvoiceCanceledException.class,()->validation.validateCanCompletePayment(invoice));
        assertEquals("No longer payment is acceptable Invoice is canceled",exception.getMessage());
    }
    private Invoice getInvoice(InvoiceStatus invoiceStatus){
        return Invoice.builder()
                .invoiceId("INV-XXX")
                .invoiceStatus(invoiceStatus)
                .updatedAt(Timestamp.from(Instant.now()))
                .build();
    }
}