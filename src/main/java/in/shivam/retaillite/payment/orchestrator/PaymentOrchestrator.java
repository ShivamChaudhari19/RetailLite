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


        //Find existing PENDING payment if present
        Payment pendingPayment=findPendingPaymentByInvoiceId(invoice.getInvoiceId());
        Payment payment;
        if (pendingPayment==null){
            //create a partial  payment with pending paymentStatus
            payment=paymentMapper.toPendingPayment(invoice,request);
        }else if (
                !(pendingPayment.getPaymentMethod()==request.paymentMethod()) || isExpired(pendingPayment)
        ){
                pendingPayment.setPaymentStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(pendingPayment);
                payment=paymentMapper.toPendingPayment(invoice,request);
        } else {
            payment=pendingPayment;
        }

        //validate Stock
        //invoiceService is separate business concern use inventory repository
        invoice.getInvoiceItems().forEach((invoiceItem -> inventoryService.validate(invoiceItem.getProduct(),invoiceItem.getQuantity())));

        PaymentService paymentService=paymentFactory.getPaymentService(request.paymentMethod().name());
        payment =paymentService.pay(payment);

        handlePayResult(payment,invoice);



//        Payment payment= paymentMapper.toPayment(request,paymentStatus,invoice);
        Payment savedPayment=paymentRepository.save(payment);
        // no need to write an external save because of Hibernate's dirty checking
        //Added to improve readability
        invoiceRepository.save(invoice);
        //todo: if the runtime exception occurred after payment is successful then
        // handle the payment at the failure

        return paymentMapper.toPaymentResponse(savedPayment);
    }

    private boolean isExpired(Payment payment) {
        Instant expirationTime=payment.getCreatedAt().toInstant().plus(paymentProperties.pending().timeout(), ChronoUnit.MINUTES);
        return !Instant.now().isBefore(expirationTime);
    }

    @Transactional
    public RefundResponse processInvoiceRefund(RefundRequest request) {

        Invoice invoice=getInvoiceByInvoiceId(request.invoiceId());

        invoiceValidation.validateCanRefund(invoice);

        Payment payment  =paymentRepository.findByInvoice_invoiceIdAndPaymentStatus(invoice.getInvoiceId(),PaymentStatus.SUCCESS)
                .orElseThrow(()->new ResourceNotFoundException("Success Payment not found!!!"));

        PaymentMethod method=payment.getPaymentMethod();
        PaymentService  paymentService=paymentFactory.getPaymentService(method.name());
        payment=paymentService.refund(payment);

        if (payment.getPaymentStatus()==PaymentStatus.REFUNDED){
            addItemStock(invoice.getInvoiceItems());
            invoice.setInvoiceStatus(InvoiceStatus.CANCELED);
        }
        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
        return  RefundResponse.builder()
                .transactionId(payment.getTransactionId())
                .invoiceId(invoice.getInvoiceId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(invoice.getGrandTotal())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private void addItemStock(List<InvoiceItem> invoiceItems) {
        invoiceItems.forEach(invoiceItem -> inventoryService.addStock(invoiceItem.getProduct(),invoiceItem.getQuantity()));
    }

    private Invoice getInvoiceByInvoiceId(String invoiceId){
        return invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(()->new ResourceNotFoundException("Invoice not found with invoice Id: "+invoiceId));
    }


    private void validateItemStock(List<InvoiceItem> invoiceItems){
        invoiceItems.forEach(invoiceItem -> inventoryService.validate(invoiceItem.getProduct(),invoiceItem.getQuantity()));

    }
    private void handlePayResult(Payment payment,Invoice invoice){
        //if paymentStatus is Success deduct the stock
        //if paymentStatus is Failed set Invoice status to pending
        //if paymentStatus is PENDING
        //if paymentStatus is REFUNDED
        if (payment.getPaymentStatus()==PaymentStatus.SUCCESS){

            log.debug("Payment Successful.....");

            invoice.setInvoiceStatus(InvoiceStatus.PAID);
            //deduct stock
            invoice.getInvoiceItems().forEach(invoiceItem -> inventoryService.deductStock(invoiceItem.getProduct(),invoiceItem.getQuantity()));

        }else if (payment.getPaymentStatus()==PaymentStatus.FAILED){
            log.debug("Payment failed......");
            invoice.setInvoiceStatus(InvoiceStatus.PENDING);
        }
    }
    private Payment findPendingPaymentByInvoiceId(String invoiceId){
        return paymentRepository.findPendingPaymentByInvoiceId(invoiceId)
                .orElse(null);
    }
}
