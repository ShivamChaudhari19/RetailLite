package in.shivam.retaillite.payment.application;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.exception.QuantityOutOfBoundException;
import in.shivam.retaillite.inventory.service.InventoryService;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.repository.InvoiceRepository;
import in.shivam.retaillite.payment.PaymentRepository;
import in.shivam.retaillite.payment.domain.validation.InvoiceValidation;
import in.shivam.retaillite.payment.dto.request.PaymentRequest;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import in.shivam.retaillite.payment.dto.request.RefundRequest;
import in.shivam.retaillite.payment.dto.response.PaymentResponse;
import in.shivam.retaillite.payment.dto.response.RefundResponse;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.exception.InvoiceAlreadyPaidException;
import in.shivam.retaillite.payment.exception.InvoiceCanceledException;
import in.shivam.retaillite.payment.exception.PaymentException;
import in.shivam.retaillite.payment.factory.PaymentServiceFactory;
import in.shivam.retaillite.payment.mapper.PaymentMapper;
import in.shivam.retaillite.payment.response.PaymentResponseFactory;
import in.shivam.retaillite.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrchestrator {
    private final PaymentRepository paymentRepository;
    private final PaymentServiceFactory paymentServiceFactory;
    private final PaymentResponseFactory paymentResponseFactory;
    private final InvoiceRepository invoiceRepository;
    private final InventoryService inventoryService;
    private final PaymentMapper paymentMapper;
    private final InvoiceValidation invoiceValidation;

    @Transactional
    public PaymentResponse processInvoicePayment(@NonNull PaymentRequest request) {

        Invoice invoice = getInvoiceByInvoiceId(request.invoiceId());

        invoiceValidation.validateCanPay(invoice);

        //validate All Item Stock
        validateItemStock(invoice.getInvoiceItems());


        //Find existing PENDING payment if present
        Payment pendingPayment = findPendingPaymentByInvoiceId(invoice.getInvoiceId());
        Payment payment;
        if (pendingPayment == null) {
            //create a partial  payment with pending paymentStatus
            payment = paymentMapper.toPendingPayment(invoice, request);
            paymentRepository.save(payment);
        } else if (!(pendingPayment.getPaymentMethod() == request.paymentMethod())  ) {
            pendingPayment.markExpired();
            paymentRepository.save(pendingPayment);
            payment = paymentMapper.toPendingPayment(invoice, request);
        } else {
            payment = pendingPayment;
        }


        PaymentService paymentService = paymentServiceFactory.getPaymentService(request.paymentMethod().name());
        payment = paymentService.pay(payment);
        try{
            completeImmediatePay(payment, invoice);
        }catch (QuantityOutOfBoundException e){
            refundPayment(payment);
            invoice.markCanceled();
        }
        return paymentResponseFactory.createResponse(payment);
    }

    @Transactional
    public RefundResponse processInvoiceRefund(RefundRequest request) {

        Invoice invoice = getInvoiceByInvoiceId(request.invoiceId());

        invoiceValidation.validateCanRefund(invoice);

        Payment payment = paymentRepository.findByInvoice_invoiceIdAndPaymentStatus(invoice.getInvoiceId(), PaymentStatus.SUCCESS)
                .orElseThrow(() -> new ResourceNotFoundException("Success Payment not found!!!"));

        PaymentMethod method = payment.getPaymentMethod();
        PaymentService paymentService = paymentServiceFactory.getPaymentService(method.name());
        payment = paymentService.refund(payment);

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            addItemStock(invoice.getInvoiceItems());
            invoice.setInvoiceStatus(InvoiceStatus.CANCELED);
        }
        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
        return RefundResponse.builder()
                .paymentId(payment.getPaymentId())
                .invoiceId(invoice.getInvoiceId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(invoice.getGrandTotal())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private void addItemStock(List<InvoiceItem> invoiceItems) {
        invoiceItems.forEach(invoiceItem -> inventoryService.addStock(invoiceItem.getProduct(), invoiceItem.getQuantity()));
    }

    private Invoice getInvoiceByInvoiceId(String invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with invoice Id: " + invoiceId));
    }


    private void validateItemStock(List<InvoiceItem> invoiceItems) {
        invoiceItems.forEach(invoiceItem -> inventoryService.validate(invoiceItem.getProduct(), invoiceItem.getQuantity()));

    }

    private void completeImmediatePay(Payment payment, Invoice invoice) {
        //if paymentStatus is Success deduct the stock
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {

            log.debug("Payment Successful.....");

            invoice.setInvoiceStatus(InvoiceStatus.PAID);
            //deduct stock
            invoice.getInvoiceItems().forEach(invoiceItem -> inventoryService.deductStock(invoiceItem.getProduct(), invoiceItem.getQuantity()));
        }
    }

    private Payment findPendingPaymentByInvoiceId(String invoiceId) {
        return paymentRepository.findPendingPaymentByInvoiceId(invoiceId)
                .orElse(null);
    }

    @Transactional
    //verify request from client
    public PaymentResponse verifyPayment(PaymentVerifyRequest request) {
        Payment payment = findPaymentByGatewayOrderId(request.gatewayOrderId());

        //idempotency
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return paymentResponseFactory.createResponse(payment);
        }


        validateCanBeVerified(payment);


        PaymentService paymentService = paymentServiceFactory.getPaymentService(payment.getPaymentMethod().name());

        payment = paymentService.verifyPayment(payment, request);
        payment= finalizePayment(payment);
        return paymentResponseFactory.createResponse(payment);
    }

    private void validateCanBeVerified(Payment payment) {
        //check for the payment is  refunded

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            log.warn("Refunded payment is trying to verify");
            throw new PaymentException("payment is Refunded...", HttpStatus.CONFLICT);
        }
    }

    private Payment findPaymentByGatewayOrderId(String gatewayOrderId) {
        return paymentRepository.findByGatewayOrderId(gatewayOrderId)
                .orElseThrow(
                        () -> new PaymentException("Payment not found for order id: " + gatewayOrderId,HttpStatus.NOT_FOUND)
                );
    }

    @Transactional
    //verify request from the razorpay webhook
    public void verifyWebhook(String paymentId, String orderId) {
        Payment payment = findPaymentByGatewayOrderId(orderId);
        payment.setGatewayPaymentId(paymentId);

        //idempotency
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return;
        }
        validateCanBeVerified(payment);
        finalizePayment(payment);
        return;
    }

    private Payment finalizePayment(Payment payment){
        Invoice invoice = payment.getInvoice();
        try{
            //check if the invoice is paid by another thread during current payment process
            invoiceValidation.validateCanCompletePayment(invoice);
            //deduct the invoice stock
            invoice.getInvoiceItems().forEach(invoiceItem -> inventoryService.deductStock(invoiceItem.getProduct(), invoiceItem.getQuantity()));
        }catch (InvoiceAlreadyPaidException e){
            log.warn("Invoice :{} is already paid so has to refund captured amount",payment.getInvoice().getInvoiceId());
            return refundPayment(payment);
        } catch (InvoiceCanceledException e){
            log.warn("Invoice :{} is canceled so has to refund captured amount",payment.getInvoice().getInvoiceId());
            return refundPayment(payment);
        }catch (ResourceNotFoundException e){
            log.warn("Some of the stocks are not available for Billing right now: error message is: {} for payment: {}",e.getMessage(),payment.getPaymentId());
            return refundPayment(payment);
        }
        catch (QuantityOutOfBoundException e) {
            log.warn("stock is over... Refund is initiated.. for payment: {}",payment.getPaymentId());
            return refundPayment(payment);
        }
        payment.markSuccess();
        invoice.markPaid();
        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
        return payment;
    }

    private Payment refundPayment(Payment payment){
        PaymentService paymentService=paymentServiceFactory.getPaymentService(payment.getPaymentMethod().name());
        payment=paymentService.refund(payment);
        if (payment.getPaymentStatus()!=PaymentStatus.REFUNDED){
            throw new PaymentException("Refund Failed for payment: "+payment.getPaymentId(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        paymentRepository.save(payment);
        return payment;
    }
}
