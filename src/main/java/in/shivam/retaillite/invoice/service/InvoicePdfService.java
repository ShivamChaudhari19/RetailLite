package in.shivam.retaillite.invoice.service;

public interface InvoicePdfService {
    byte[] generateInvoice(String invoiceId);
}
