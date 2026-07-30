package in.shivam.retaillite.invoice.service.impl;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.mapper.InvoicePdfMapper;

import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;

import in.shivam.retaillite.invoice.pdf.generator.PdfGenerator;
import in.shivam.retaillite.invoice.repository.InvoiceRepository;
import in.shivam.retaillite.invoice.service.InvoicePdfService;
import in.shivam.retaillite.payment.PaymentRepository;
import in.shivam.retaillite.payment.domain.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private final CompanyProperties companyProperties;

    private final InvoiceRepository invoiceRepository;

    private final InvoicePdfMapper invoicePdfMapper;

    private final PdfGenerator pdfGenerator;
    private final PaymentRepository paymentRepository;

    @Override
    public byte[] generateInvoice(String invoiceId) {

        Invoice invoice =
                invoiceRepository.findByInvoiceId(invoiceId)
                        .orElseThrow(()->new ResourceNotFoundException("Invoice not found"));

        Payment payment;
        if (invoice.getInvoiceStatus()== InvoiceStatus.PENDING){
            payment=null;
        }else if (invoice.getInvoiceStatus()==InvoiceStatus.PAID){
            payment=paymentRepository.findByInvoice_invoiceIdAndPaymentStatus(invoiceId, PaymentStatus.SUCCESS)
                    .orElseThrow(()->new ResourceNotFoundException("Successful payment not found for Invoice ID: "+invoiceId));
        }else if (invoice.getInvoiceStatus()==InvoiceStatus.CANCELED){
            payment=paymentRepository.findByInvoice_invoiceIdAndPaymentStatus(invoiceId,PaymentStatus.REFUNDED)
                    .orElseThrow(()->new ResourceNotFoundException("Refunded payment not Found for Invoice id: "+invoiceId));
        }else {
            payment=null;
        }

        InvoiceDocument document =
                invoicePdfMapper.toDocument(invoice,payment);

        return pdfGenerator.generate(
                document,
                companyProperties
        );
    }
}