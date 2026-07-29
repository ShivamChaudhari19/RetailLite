package in.shivam.retaillite.payment.response;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.payment.config.RazorpayProperties;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.dto.response.CashPaymentResponse;
import in.shivam.retaillite.payment.dto.response.PaymentResponse;
import in.shivam.retaillite.payment.dto.response.RazorpayPaymentResponse;
import in.shivam.retaillite.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResponseFactory {
    private final RazorpayProperties razorpayProperties;
    public PaymentResponse createResponse(Payment payment){
        if (payment.getPaymentMethod()== PaymentMethod.CASH){
            return cashPaymentResponse(payment);
        }else if (payment.getPaymentMethod()==PaymentMethod.ONLINE){
            return razorpayPaymentResponse(payment);
        }
        throw new PaymentException("Illegal payment method...", HttpStatus.BAD_REQUEST);
    }

    private CashPaymentResponse cashPaymentResponse(Payment payment){
        return CashPaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .invoiceId(payment.getInvoice().getInvoiceId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getInvoice().getGrandTotal())
                .paymentStatus(payment.getPaymentStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
    private RazorpayPaymentResponse razorpayPaymentResponse(Payment payment){
        return RazorpayPaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .invoiceId(payment.getInvoice().getInvoiceId())
                .paymentMethod(payment.getPaymentMethod())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayKeyId(razorpayProperties.keyId())
                .currency("INR")
                .amount(payment.getInvoice().getGrandTotal())
                .paymentStatus(payment.getPaymentStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
