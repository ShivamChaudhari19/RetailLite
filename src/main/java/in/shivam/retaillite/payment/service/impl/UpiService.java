package in.shivam.retaillite.payment.service.impl;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.payment.dto.PaymentRequest;
import in.shivam.retaillite.payment.dto.PaymentResponse;
import in.shivam.retaillite.payment.service.PaymentService;

import java.math.BigDecimal;

public class UpiService implements PaymentService {

    private static final String PAYMENT_METHOD="UPI";
    @Override
    public PaymentStatus pay(BigDecimal amount) {
        //todo: Add UPI payment service
        return null;
    }

    @Override
    public String getPaymentMethod() {
        return PAYMENT_METHOD;
    }
}
