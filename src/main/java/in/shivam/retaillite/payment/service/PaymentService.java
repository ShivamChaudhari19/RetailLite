package in.shivam.retaillite.payment.service;




import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.payment.dto.PaymentRequest;
import in.shivam.retaillite.payment.dto.PaymentResponse;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentResponse pay(PaymentRequest request);
}
