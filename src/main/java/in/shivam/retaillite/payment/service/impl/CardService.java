package in.shivam.retaillite.payment.service.impl;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.payment.dto.PaymentRequest;
import in.shivam.retaillite.payment.dto.PaymentResponse;
import in.shivam.retaillite.payment.entity.Payment;
import in.shivam.retaillite.payment.service.PaymentService;

import java.math.BigDecimal;

public class CardService implements PaymentService {
    private static final String PAYMENT_METHOD="CARD";


    @Override
    public PaymentStatus pay(BigDecimal amount) {
        //todo: add CARD payment service
        return null;
    }

    @Override
    public String getPaymentMethod() {
        return PAYMENT_METHOD;
    }
}
