package in.shivam.retaillite.invoice.service;

import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import org.springframework.data.domain.Page;

public interface InvoiceService {
    InvoiceResponse createInvoice(InvoiceRequest request);
    Page<InvoiceResponse> findByInvoiceStatus(
            Integer page,
            Integer size,
            String  invoiceStatus
    );
    Page<InvoiceResponse> findAll(
            Integer page,
            Integer size,
            String sortBy,
            String orderedBy
    );

    InvoiceResponse findInvoice(String invoiceId);
}
