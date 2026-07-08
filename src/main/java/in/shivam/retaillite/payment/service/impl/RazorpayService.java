package in.shivam.retaillite.payment.service.impl;

import com.razorpay.Order;
import com.razorpay.Refund;
import in.shivam.retaillite.payment.config.RazorpayProperties;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.exception.PaymentException;
import in.shivam.retaillite.payment.exception.PaymentVerificationException;
import in.shivam.retaillite.payment.gateway.RazorpayGateway;
import in.shivam.retaillite.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayService implements PaymentService {
    private static final String PAYMENT_METHOD="ONLINE";
    private final RazorpayGateway gateway;
    private final RazorpayProperties razorpayProperties;

    @Override
    public Payment pay(Payment payment) {

        if (payment.getGatewayOrderId()!=null){
            if (!isOrderIdExpired(payment))
                return payment;
        }
        Order order= gateway.createOrder(payment);
        String gatewayOrderId= order.get("id");
        payment.setGatewayOrderId(gatewayOrderId);
        payment.setGatewayOrderIdCreatedAt(Timestamp.from(Instant.now()));
        return payment;
    }

    private boolean isOrderIdExpired(Payment payment) {
        Instant orderIdExpiredAt=payment.getGatewayOrderIdCreatedAt().toInstant().plus(razorpayProperties.timeout(), ChronoUnit.MINUTES );
        return Instant.now().isAfter(orderIdExpiredAt);
    }

    @Override
    public Payment refund(Payment payment) {
        Refund refund=gateway.refundPayment(payment);
        if (!"processed".equalsIgnoreCase(refund.get("status"))){
            log.warn("payment failed for the payment: {}",payment.getPaymentId());
            throw new PaymentException("Refund Failed for payment id: "+payment.getPaymentId(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        payment.setGatewayRefundId(refund.get("id"));
        payment.markRefunded();
        return payment;
    }

    @Override
    public String getPaymentMethod() {
        return PAYMENT_METHOD;
    }

    @Override
    public Payment verifyPayment(Payment payment, PaymentVerifyRequest request) {
        boolean verified =gateway.verifySignature(request.gatewayPaymentId(),request.gatewayOrderId(),request.signature());
        if (!verified){
            log.warn("payment varification failed....");
            throw new PaymentVerificationException("payment varification failed..");
        }
        payment.setGatewayPaymentId(request.gatewayPaymentId());

        return payment;

    }

}
