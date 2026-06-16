package in.shivam.retaillite.payment.dto;

import in.shivam.retaillite.common.enums.PaymentMethod;

public record PaymentRequest(
        String invoiceId,
        PaymentMethod paymentMethod
) {}
