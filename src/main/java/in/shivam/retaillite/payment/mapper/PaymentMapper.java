package in.shivam.retaillite.payment.mapper;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.payment.dto.PaymentRequest;
import in.shivam.retaillite.payment.dto.PaymentResponse;
import in.shivam.retaillite.payment.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentMapper {
    public  Payment toPendingPayment(Invoice invoice, PaymentRequest request) {
        return Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .invoice(invoice)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    public PaymentResponse toPaymentResponse(Payment savedPayment) {
        return PaymentResponse.builder()
                .transactionId(savedPayment.getPaymentId())
                .invoiceId(savedPayment.getInvoice().getInvoiceId())
                .paymentMethod(savedPayment.getPaymentMethod())
                .paymentStatus(savedPayment.getPaymentStatus())
                .amount(savedPayment.getInvoice().getGrandTotal())
                .createdAt(savedPayment.getCreatedAt())
                .build();
    }

    public Payment toPayment(
            PaymentRequest request,
            PaymentStatus paymentStatus,
            Invoice invoice
    ) {
        return Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .invoice(invoice)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(paymentStatus)
                .build();
    }
}
