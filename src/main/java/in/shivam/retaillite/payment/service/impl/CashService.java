package in.shivam.retaillite.payment.service.impl;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import in.shivam.retaillite.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService implements PaymentService {
    private static final String PAYMENT_METHOD="CASH";

    @Override
    public Payment pay(Payment payment) {
        payment.markSuccess();
        return payment;
    }

    @Override
    public Payment refund(Payment payment) {
        payment.markRefunded();
        return payment;
    }

    @Override
    public String getPaymentMethod() {
        log.debug("Payment method is: {}",PAYMENT_METHOD);
        return PAYMENT_METHOD;
    }

    @Override
    public Payment verifyPayment(Payment payment, PaymentVerifyRequest request) {
        return payment;
    }
}
