package in.shivam.retaillite.payment.service.impl;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {
    private final CashService cashService=new CashService();
    @Test
    void pay_shouldMarkSUCCESS_ForPENDINGPayment() {
        Payment payment=getPayment(PaymentStatus.PENDING);
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());

        payment=cashService.pay(payment);
        assertEquals(PaymentStatus.SUCCESS,payment.getPaymentStatus());
    }

    @Test
    void refund_shouldMarkREFUNDED_ForSUCCESSPayment() {
        Payment payment=getPayment(PaymentStatus.SUCCESS);
        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());

        payment=cashService.refund(payment);
        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
    }

    @Test
    void getPaymentMethod() {
        String paymentMethod=cashService.getPaymentMethod();
        assertEquals("CASH",paymentMethod);
    }

    @Test
    void verifyPayment() {
        Payment payment=getPayment(PaymentStatus.SUCCESS);
        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        PaymentVerifyRequest request=new PaymentVerifyRequest(null,null,null);
        payment=cashService.verifyPayment(payment,request);
        assertEquals(PaymentStatus.SUCCESS,payment.getPaymentStatus());
    }

    private Payment getPayment(PaymentStatus status){
        return Payment.builder()
                .paymentId("paymentId-XXXX")
                .paymentMethod(PaymentMethod.CASH)
                .paymentStatus(status)
                .build();
    }
}