package in.shivam.retaillite.payment.service.impl;


import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.service.InventoryService;
import in.shivam.retaillite.invoice.InvoiceRepository;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.payment.InvoiceAlreadyPaidException;
import in.shivam.retaillite.payment.PaymentRepository;
import in.shivam.retaillite.payment.dto.PaymentRequest;
import in.shivam.retaillite.payment.dto.PaymentResponse;
import in.shivam.retaillite.payment.entity.Payment;
import in.shivam.retaillite.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        Invoice invoice=getInvoice(request.invoiceId());

        if (invoice.getInvoiceStatus()==InvoiceStatus.PAID){
            throw new InvoiceAlreadyPaidException("Bill is Already Paid");
        }
        if (request.paymentMethod()==PaymentMethod.CASH){
            Payment payment=toPayment(request,PaymentStatus.SUCCESS,invoice);
            invoice.setInvoiceStatus(InvoiceStatus.PAID);

            //deduct the stock before the payment.
            invoice.getInvoiceItems()
                            .forEach((invoiceItem)->inventoryService.validate(invoiceItem.getProduct(),invoiceItem.getQuantity()));
            invoice.getInvoiceItems()
                            .forEach((invoiceItem -> inventoryService.deductStock(invoiceItem.getProduct(),invoiceItem.getQuantity())));

            invoiceRepository.save(invoice);
            Payment savedPayment=paymentRepository.save(payment);
            String message="Amount Paid by Cash" +
                    "Invoice is Generated";
            return toPaymentResponse(savedPayment,message);
        }
        if (request.paymentMethod()==PaymentMethod.ONLINE){
            //todo: implement razorpay
        }
        throw new RestClientException("Plz select valid payment option");
    }
    private PaymentResponse toPaymentResponse(Payment savedPayment,String message) {
        return PaymentResponse.builder()
                .transactionId(savedPayment.getTransactionId()).invoiceId(savedPayment.getInvoice().getInvoiceId()).paymentmethod(savedPayment.getPaymentmethod()).paymentStatus(savedPayment.getPaymentStatus()).amount(savedPayment.getInvoice().getGrandTotal()).createdAt(savedPayment.getCreatedAt()).message(message)
                .build();
    }

    private Payment toPayment(PaymentRequest request,PaymentStatus paymentStatus, Invoice invoice) {
        return Payment.builder()
                .transactionId(UUID.randomUUID().toString())
                .invoice(invoice)
                .paymentmethod(request.paymentMethod())
                .paymentStatus(paymentStatus)
                .build();
    }

    private Invoice getInvoice(String invoiceId){
        return invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(()-> new ResourceNotFoundException("Invoice does not Exists."));

    }
}
