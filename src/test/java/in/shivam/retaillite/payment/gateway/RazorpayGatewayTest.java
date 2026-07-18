package in.shivam.retaillite.payment.gateway;

import com.razorpay.*;
import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.payment.config.RazorpayProperties;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.exception.PaymentException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RazorpayGatewayTest {
    @Mock
    private RazorpayClient razorpayClient;
    RazorpayGateway razorpayGateway;
    @BeforeEach
    void setUp(){
        RazorpayProperties razorpayProperties=new RazorpayProperties(
                "test-key-id",
                "test-demo-secret-id",
                2L,
                "webhookSecret"
        );
        razorpayGateway=new RazorpayGateway(razorpayClient,razorpayProperties);
    }

    @Test
    void createOrder_WhenPaymentIsValid_shouldCreateOrder() throws RazorpayException {
        Payment payment=getPayment(PaymentStatus.PENDING);
        Order order=mock(Order.class);
        OrderClient orderClient=mock(OrderClient.class);
        razorpayClient.orders=orderClient;
        when(orderClient.create(any(JSONObject.class))).thenReturn(order);
        when(order.get("id")).thenReturn("Ord_XXXXX");
        assertDoesNotThrow(()->razorpayGateway.createOrder(payment));
        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor=ArgumentCaptor.forClass(JSONObject.class);
        verify(orderClient).create(jsonObjectArgumentCaptor.capture());
        JSONObject jsonObject=jsonObjectArgumentCaptor.getValue();
        assertEquals(10000L,jsonObject.get("amount"));
        assertEquals("INR",jsonObject.get("currency"));
        assertEquals(payment.getPaymentId(),jsonObject.get("receipt"));

    }
    @Test
    void createOrder_WhenRazorpayException_shouldThrowPaymentExceptionWithStatus500() throws RazorpayException {
        Payment payment=getPayment(PaymentStatus.PENDING);
        OrderClient orderClient=mock(OrderClient.class);
        razorpayClient.orders=orderClient;
        when(orderClient.create(any(JSONObject.class))).thenThrow(RazorpayException.class);

        assertThrows(PaymentException.class,()->razorpayGateway.createOrder(payment));


        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor=ArgumentCaptor.forClass(JSONObject.class);

        verify(orderClient).create(jsonObjectArgumentCaptor.capture());

        JSONObject jsonObject=jsonObjectArgumentCaptor.getValue();
        assertEquals(10000L,jsonObject.get("amount"));
        assertEquals("INR",jsonObject.get("currency"));
        assertEquals(payment.getPaymentId(),jsonObject.get("receipt"));
    }

    @Test
    void verifySignature_WhenPaymentIsCapturedAndSignatureIsValid_ShouldReturnTrue() throws RazorpayException {
        String paymentId="raz_paymentId_XXXXX";
        String orderId="raz_order_id_XXXXX";
        String signature="raz_signature";
        com.razorpay.Payment razorpay_payment=mock(com.razorpay.Payment.class);
        PaymentClient paymentClient=mock(PaymentClient.class);
        razorpayClient.payments=paymentClient;
        when(paymentClient.fetch(anyString())).thenReturn(razorpay_payment);
        when(razorpay_payment.get("status")).thenReturn("captured");
        try(MockedStatic<Utils>mockedStatic=mockStatic(Utils.class)){
            mockedStatic.when(()->Utils.verifyPaymentSignature(any(),anyString())).thenReturn(true);
            boolean result= razorpayGateway.verifySignature(paymentId,orderId,signature);
            assertTrue(result);
            ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor=ArgumentCaptor.forClass(JSONObject.class);
            ArgumentCaptor<String> stringArgumentCaptor=ArgumentCaptor.forClass(String.class);
            mockedStatic.verify(()->Utils.verifyPaymentSignature(jsonObjectArgumentCaptor.capture(),stringArgumentCaptor.capture()));
            JSONObject jsonObject=jsonObjectArgumentCaptor.getValue();
            assertEquals(paymentId,jsonObject.get("razorpay_payment_id"));
            assertEquals(orderId,jsonObject.get("razorpay_order_id"));
            assertEquals(signature,jsonObject.get("razorpay_signature"));
        }

    }
    @Test
    void verifySignature_WhenPaymentIsNotCapturedAndSignatureIsValid_ShouldReturnFalse() throws RazorpayException {
        String paymentId="raz_paymentId_XXXXX";
        String orderId="raz_order_id_XXXXX";
        String signature="raz_signature";
        com.razorpay.Payment razorpay_payment=mock(com.razorpay.Payment.class);
        PaymentClient paymentClient=mock(PaymentClient.class);
        razorpayClient.payments=paymentClient;
        when(paymentClient.fetch(anyString())).thenReturn(razorpay_payment);
        when(razorpay_payment.get("status")).thenReturn("not captured");
        boolean result= razorpayGateway.verifySignature(paymentId,orderId,signature);
        assertFalse(result);

    }

    @Test
    void verifySignature_WhenPaymentIsCapturedAndSignatureIsNotValid_ShouldThrowRazorpayException() throws RazorpayException {
        String paymentId="raz_paymentId_XXXXX";
        String orderId="raz_order_id_XXXXX";
        String signature="raz_signature";
        com.razorpay.Payment razorpay_payment=mock(com.razorpay.Payment.class);
        PaymentClient paymentClient=mock(PaymentClient.class);
        razorpayClient.payments=paymentClient;
        when(paymentClient.fetch(anyString())).thenReturn(razorpay_payment);
        when(razorpay_payment.get("status")).thenReturn("captured");
        try(MockedStatic<Utils>mockedStatic=mockStatic(Utils.class)){
            mockedStatic.when(()->Utils.verifyPaymentSignature(any(),anyString())).thenThrow(RazorpayException.class);
            assertThrows(PaymentException.class,()->razorpayGateway.verifySignature(paymentId,orderId,signature));

            ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor=ArgumentCaptor.forClass(JSONObject.class);
            ArgumentCaptor<String> stringArgumentCaptor=ArgumentCaptor.forClass(String.class);
            mockedStatic.verify(()->Utils.verifyPaymentSignature(jsonObjectArgumentCaptor.capture(),stringArgumentCaptor.capture()));
            JSONObject jsonObject=jsonObjectArgumentCaptor.getValue();
            assertEquals(paymentId,jsonObject.get("razorpay_payment_id"));
            assertEquals(orderId,jsonObject.get("razorpay_order_id"));
            assertEquals(signature,jsonObject.get("razorpay_signature"));
        }

    }
    @Test
    void refundPayment_WhenRefundIsProcessed_ShouldReturnRefundId() throws RazorpayException {
        Payment payment=getPayment(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId("raz_payment_id_XXXXX");
        Refund refund=mock(Refund.class);
       PaymentClient paymentClient=mock(PaymentClient.class);
       razorpayClient.payments=paymentClient;
       when(paymentClient.refund(anyString(),any(JSONObject.class ))).thenReturn(refund);
       when(refund.get("status")).thenReturn("processed");
       when(refund.get("id")).thenReturn("raz_refund_id_XXXXX");
       assertDoesNotThrow(()->razorpayGateway.refundPayment(payment));
       ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor= ArgumentCaptor.forClass(JSONObject.class);
       verify(paymentClient).refund(anyString(),jsonObjectArgumentCaptor.capture());
       JSONObject jsonObject=jsonObjectArgumentCaptor.getValue();
       assertEquals(10000L,jsonObject.get("amount"));

    }
    @Test
    void refundPayment_WhenRefundIsNotProcessed_ShouldThrowPaymentException() throws RazorpayException {
        Payment payment=getPayment(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId("raz_payment_id_XXXXX");
        Refund refund=mock(Refund.class);
        PaymentClient paymentClient=mock(PaymentClient.class);
        razorpayClient.payments=paymentClient;
        when(paymentClient.refund(anyString(),any(JSONObject.class ))).thenReturn(refund);
        when(refund.get("status")).thenReturn("failed");
        PaymentException exception=assertThrows(PaymentException.class,()->razorpayGateway.refundPayment(payment));
        assertEquals("Refund Failed for payment id: "+payment.getPaymentId(),exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,exception.getStatusCode());

        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor= ArgumentCaptor.forClass(JSONObject.class);
        verify(paymentClient).refund(anyString(),jsonObjectArgumentCaptor.capture());
        JSONObject jsonObject=jsonObjectArgumentCaptor.getValue();
        assertEquals(10000L,jsonObject.get("amount"));

    }

    @Test
    void refundPayment_WhenRazorpayClientThrowsException_ShouldThrowPaymentException() throws RazorpayException {
        Payment payment=getPayment(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId("raz_payment_id_XXXXX");
        PaymentClient paymentClient=mock(PaymentClient.class);
        razorpayClient.payments=paymentClient;
        when(paymentClient.refund(anyString(),any(JSONObject.class ))).thenThrow(RazorpayException.class);

        PaymentException exception=assertThrows(PaymentException.class,()->razorpayGateway.refundPayment(payment));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,exception.getStatusCode());

        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor= ArgumentCaptor.forClass(JSONObject.class);
        verify(paymentClient).refund(anyString(),jsonObjectArgumentCaptor.capture());
        JSONObject jsonObject=jsonObjectArgumentCaptor.getValue();
        assertEquals(10000L,jsonObject.get("amount"));

    }
    private Payment getPayment(PaymentStatus status){
        return Payment.builder()
                .paymentId("paymentId-XXXX")
                .invoice(Invoice.builder().grandTotal(BigDecimal.valueOf(100L)).build())
                .paymentMethod(PaymentMethod.CASH)
                .paymentStatus(status)
                .build();
    }
}