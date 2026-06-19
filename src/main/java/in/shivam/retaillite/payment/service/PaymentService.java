package in.shivam.retaillite.payment.service;




import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.payment.dto.PaymentRequest;
import in.shivam.retaillite.payment.dto.PaymentResponse;
import in.shivam.retaillite.payment.entity.Payment;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentStatus pay(BigDecimal amount);
    String getPaymentMethod();
}
