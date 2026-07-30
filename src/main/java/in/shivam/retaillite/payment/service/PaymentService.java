package in.shivam.retaillite.payment.service;


import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;

public interface PaymentService {
    Payment pay(Payment payment);
    Payment  refund(Payment payment);
    String getPaymentMethod();

    Payment verifyPayment(Payment payment, PaymentVerifyRequest request);
}
