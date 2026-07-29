package in.shivam.retaillite.payment.gateway;

import com.razorpay.*;
import in.shivam.retaillite.payment.config.RazorpayProperties;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Component
public class RazorpayGateway {
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;

    private static final String CURRENCY="INR";
    public String createOrder(Payment payment){
        //razorpay required amount in Paisa not in rupee
        long amountInPaisa=extractAmountInPaisa(payment.getInvoice().getGrandTotal());
        //razorpay only support INR currency


        JSONObject orderRequest=getOrderRequest(amountInPaisa, payment.getPaymentId());

        try {
            Order order= razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            throw new PaymentException("razorpay order creation is failed due to:"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean verifySignature(
            String paymentId,
            String orderId,
            String signature
    ){

        try {
            JSONObject attributes =new JSONObject();
            attributes.put("razorpay_order_id",orderId);
            attributes.put("razorpay_payment_id",paymentId);
            attributes.put("razorpay_signature",signature);
            com.razorpay.Payment razorpay_payment=razorpayClient.payments.fetch(paymentId);
            String status=razorpay_payment.get("status");
            return "captured".equalsIgnoreCase(status) &&
                    Utils.verifyPaymentSignature(attributes, razorpayProperties.keySecret());
        } catch (RazorpayException e) {
            log.warn("Razor pay error: {} for orderId: {}",e.getMessage(),orderId);
            throw new PaymentException(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private long extractAmountInPaisa(BigDecimal amount){
        return amount
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
    }
    private JSONObject getOrderRequest(long amountInPaisa, String paymentId){
        //create JSONObject
        JSONObject requestOrder=new JSONObject();
        requestOrder.put("amount",amountInPaisa);
        requestOrder.put("currency", RazorpayGateway.CURRENCY);
        requestOrder.put("receipt",paymentId);
        return requestOrder;
    }

    public String refundPayment(Payment payment) {
        JSONObject request=new JSONObject();
        request.put("amount",extractAmountInPaisa(payment.getInvoice().getGrandTotal()));
        try{
            Refund refund= razorpayClient.payments.refund(payment.getGatewayPaymentId(),request  );
            if (!"processed".equalsIgnoreCase(refund.get("status"))){
                log.warn("payment failed for the payment: {}",payment.getPaymentId());
                throw new PaymentException("Refund Failed for payment id: "+payment.getPaymentId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return refund.get("id");
        } catch (RazorpayException e) {
            throw new PaymentException(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
