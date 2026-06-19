package in.shivam.retaillite.payment.service.impl;

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
import in.shivam.retaillite.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService implements PaymentService {
    private static final String PAYMENT_METHOD="CASH";

    private final InvoiceRepository invoiceRepository;
    private final InventoryService inventoryService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

//    @Override
//    @Transactional
//    public PaymentResponse pay(PaymentRequest request) {
//
//        //Check the invoice is valid or not
//        Invoice invoice= invoiceRepository.findByInvoiceId(request.invoiceId())
//                .orElseThrow(()->new ResourceNotFoundException("Invoice not found for invoiceId: "+request.invoiceId()));
//
//        //Check if the Invoice Bill is already paid
//        if (invoice.getInvoiceStatus().name().equals("PAID")){
//            log.warn("Bill is already Paid for Invoice: {} ",invoice.getInvoiceId());
//            throw new InvoiceAlreadyPaidException("Bill is Already Paid");
//        }
//
//        //Validate stock
//        invoice.getInvoiceItems().forEach((invoiceItem -> inventoryService.validate(invoiceItem.getProduct(),invoiceItem.getQuantity())));
//
//        //deduct stock
//        invoice.getInvoiceItems().forEach((invoiceItem -> inventoryService.deductStock(invoiceItem.getProduct(),invoiceItem.getQuantity())));
//
//        //pay the bill
//        Payment payment= paymentMapper.toPayment(request, PaymentStatus.SUCCESS,invoice);
//        Payment savedPayment=paymentRepository.save(payment);
//
//        //save Invoice with PAID status
//        invoice.setInvoiceStatus(InvoiceStatus.PAID);
//        invoiceRepository.save(invoice);
//
//        log.debug("CASH payment successful...");
//
//        return paymentMapper.toPaymentResponse(savedPayment, "Bill Paid Successfully...");
//    }

    @Override
    public PaymentStatus pay(BigDecimal amount) {
        return PaymentStatus.SUCCESS;
    }

    @Override
    public String getPaymentMethod() {
        log.debug("Payment method is: {}",PAYMENT_METHOD);
        return PAYMENT_METHOD;
    }
}
