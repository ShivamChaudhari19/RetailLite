package in.shivam.retaillite.payment.domain.validation;

import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.payment.exception.InvoiceAlreadyPaidException;
import in.shivam.retaillite.payment.exception.InvoiceCanceledException;
import in.shivam.retaillite.payment.exception.InvoiceNotPaidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InvoiceValidation {

    public void validateCanPay(Invoice invoice){
        //check if the invoice is already paid
        if (invoice.getInvoiceStatus()==InvoiceStatus.PAID){
            log.debug("Invoice is already paid for Invoice Id: {}", invoice.getInvoiceId());
            throw new InvoiceAlreadyPaidException("Invoice is already paid....");
        }

        //check if the invoice is canceled
        if (invoice.getInvoiceStatus()==InvoiceStatus.CANCELED){
            log.debug("Invoice is canceled do not process Invoice");
            throw new InvoiceCanceledException("This Invoice is canceled...");
        }
    }

    public void validateCanRefund(Invoice invoice){
        if (invoice.getInvoiceStatus()!= InvoiceStatus.PAID){
            throw  new InvoiceNotPaidException("Invoice:"+invoice.getInvoiceId()+" is not Paid, Refund Cannot Possible ");
        }
    }
    public void validateCanCompletePayment(Invoice invoice){
        if (invoice.getInvoiceStatus()==InvoiceStatus.PAID){
            throw  new InvoiceAlreadyPaidException("Invoice: "+invoice.getInvoiceId()+" is already paid at: "+invoice.getUpdatedAt());
        }
        if (invoice.getInvoiceStatus()==InvoiceStatus.CANCELED){
            throw new InvoiceCanceledException("No longer payment is acceptable Invoice is canceled");
        }
    }
}
