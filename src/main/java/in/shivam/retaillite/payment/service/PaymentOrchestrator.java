package in.shivam.retaillite.payment.service;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.inventory.service.InventoryService;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.repository.InvoiceRepository;
import in.shivam.retaillite.payment.InvoiceAlreadyPaidException;
import in.shivam.retaillite.payment.PaymentRepository;
import in.shivam.retaillite.payment.dto.PaymentRequest;
import in.shivam.retaillite.payment.dto.PaymentResponse;
import in.shivam.retaillite.payment.entity.Payment;
import in.shivam.retaillite.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrchestrator {
    private final PaymentRepository paymentRepository;
    private final PaymentFactory paymentFactory;
    private final InvoiceRepository invoiceRepository;
    private final InventoryService inventoryService;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse processInvoicePayment(PaymentRequest request){

        Invoice invoice=invoiceRepository.findByInvoiceId(request.invoiceId())
                .orElseThrow(()->new ResourceNotFoundException("Invoice not found with invoice Id: "+request.invoiceId()));

        //check if the invoice is already paid
        if ("PAID".equals(invoice.getInvoiceStatus().name())){
            log.debug("Invoice is already paid for Invoice Id: {}", invoice.getInvoiceId());
            throw new InvoiceAlreadyPaidException("Invoice is already paid....");
        }

        //check if the invoice is canceled
        if ("CANCELED".equals(invoice.getInvoiceStatus().name())){
            log.debug("Invoice is canceled do not process Invoice");
            throw new IllegalStateException("This Invoice is canceled...");
        }

        //validate Stock
        //invoiceService is separate business concern use inventory repository
        invoice.getInvoiceItems().forEach((invoiceItem -> inventoryService.validate(invoiceItem.getProduct(),invoiceItem.getQuantity())));

        PaymentService paymentService=paymentFactory.getPaymentService(request.paymentMethod().name());

        PaymentStatus paymentStatus =paymentService.pay(invoice.getGrandTotal());

        //if paymentStatus is Success deduct the stock
        //if paymentStatus is Failed set Invoice status to pending
        //if paymentStatus is PENDING
        //if paymentStatus is REFUNDED
        if ("SUCCESS".equals(paymentStatus.name())){

            log.debug("Payment Successful.....");

            invoice.setInvoiceStatus(InvoiceStatus.PAID);
            //deduct stock
            invoice.getInvoiceItems().forEach(invoiceItem -> inventoryService.deductStock(invoiceItem.getProduct(),invoiceItem.getQuantity()));

        }else if ("FAILED".equals(paymentStatus.name())){
            log.debug("Payment failed......");
            invoice.setInvoiceStatus(InvoiceStatus.PENDING);
        }

        Payment payment= paymentMapper.toPayment(request,paymentStatus,invoice);
        Payment savedPayment=paymentRepository.save(payment);
        invoiceRepository.save(invoice);

        return paymentMapper.toPaymentResponse(savedPayment);





    }
}
