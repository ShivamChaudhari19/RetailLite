package in.shivam.retaillite.payment.mapper;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.dto.request.PaymentRequest;
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
}
