package in.shivam.retaillite.payment.service.impl;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.payment.config.RazorpayProperties;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import in.shivam.retaillite.payment.exception.PaymentException;
import in.shivam.retaillite.payment.exception.PaymentVerificationException;
import in.shivam.retaillite.payment.gateway.RazorpayGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RazorpayServiceTest {

    @Mock
    private RazorpayGateway gateway;
    private RazorpayService razorpayService;
    @BeforeEach
    void setUp() {
        RazorpayProperties razorpayProperties=new RazorpayProperties(
                "test-key-id",
                "test-demo-secret-id",
                2L,
                "webhookSecret"
        );
        razorpayService=new RazorpayService(gateway,razorpayProperties);
    }

    @Test
    void pay_shouldReturnGatewayOrderId_WithPENDINGStatus() {
        Payment payment=getPayment(PaymentStatus.PENDING);
        when(gateway.createOrder(payment)).thenReturn("ord_XXXXX");
        payment=razorpayService.pay(payment);
        assertEquals("ord_XXXXX",payment.getGatewayOrderId());
        assertEquals(PaymentStatus.PENDING,payment.getPaymentStatus());
    }

    @Test
    void pay_WhenGatewayThrowsException_ShouldSetOrderId() {
        Payment payment=getPayment(PaymentStatus.PENDING);
        when(gateway.createOrder(payment)).thenThrow(PaymentException.class);
        assertThrows(PaymentException.class,()->razorpayService.pay(payment));
        assertNull(payment.getGatewayOrderId());
    }
    @Test
    void pay_shouldReturnExistingGatewayOrderId_WithPENDINGStatus_IfOrderIdIsNotExpired() {
        Payment payment=getPayment(PaymentStatus.PENDING);
        payment.setGatewayOrderId("ord_YYYYY");
        payment.setGatewayOrderIdCreatedAt(new Timestamp(System.currentTimeMillis()));

        payment=razorpayService.pay(payment);
        assertEquals("ord_YYYYY",payment.getGatewayOrderId());
        assertEquals(PaymentStatus.PENDING,payment.getPaymentStatus());
    }
    @Test
    void pay_shouldReturnNewGatewayOrderId_WithPENDINGStatus_IfOrderIdIsExpired() {
        Payment payment=getPayment(PaymentStatus.PENDING);
        payment.setGatewayOrderId("ord_YYYYY");
        payment.setGatewayOrderIdCreatedAt(Timestamp.from(Instant.now().minus(3, ChronoUnit.MINUTES)));
        when(gateway.createOrder(payment)).thenReturn("ord_XXXXX");

        payment=razorpayService.pay(payment);
        assertEquals("ord_XXXXX",payment.getGatewayOrderId());
        assertEquals(PaymentStatus.PENDING,payment.getPaymentStatus());
    }
    @Test
    void refund_shouldRefund_WithPEFUNDEDStatus() {
        Payment payment=getPayment(PaymentStatus.SUCCESS);
        when(gateway.refundPayment(payment)).thenReturn("refund_XXXX");
        payment=razorpayService.refund(payment);
        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals("refund_XXXX",payment.getGatewayRefundId());
    }
    @Test
    void refund_WhenGatewayThrowsException_ShouldNotModifyPaymentStatus() {
        // Arrange
        Payment payment = getPayment(PaymentStatus.SUCCESS); // Assuming it starts as SUCCESS

        // Simulate the gateway failing
        when(gateway.refundPayment(payment))
                .thenThrow(new PaymentException("Refund Failed", HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        // 1. Verify that the exception is passed through the service
        assertThrows(PaymentException.class, () -> {
            razorpayService.refund(payment);
        });

        // 2. Verify that the payment state was NOT modified due to the failure
        assertNotEquals(PaymentStatus.REFUNDED, payment.getPaymentStatus());
        assertNull(payment.getGatewayRefundId());
    }

    @Test
    void getPaymentMethod_ShouldReturnPaymentMethod() {
        assertEquals("ONLINE",razorpayService.getPaymentMethod());
    }

    @Test
    void verifyPayment_WhenSignatureIsVarified_ShouldSavePaymentId() {

        Payment payment = getPayment(PaymentStatus.PENDING);
        PaymentVerifyRequest request = new PaymentVerifyRequest("ord_XXXXX", "payment_XXXXX", "sign_XXXX");
        when(gateway.verifySignature(anyString(), anyString(), anyString())).thenReturn(true);
        payment = razorpayService.verifyPayment(payment, request);
        assertEquals(request.gatewayPaymentId(), payment.getGatewayPaymentId());

    }
    @Test
    void verifyPayment_WhenSignatureIsNotVarified_ShouldThrowExceptionAndNotSavePaymentId() {

        Payment payment = getPayment(PaymentStatus.PENDING);
        PaymentVerifyRequest request = new PaymentVerifyRequest("ord_XXXXX", "payment_XXXXX", "sign_XXXX");
        when(gateway.verifySignature(anyString(), anyString(), anyString())).thenReturn(false);
        assertThrows(PaymentVerificationException.class,()->razorpayService.verifyPayment(payment, request));
        assertNull(payment.getGatewayPaymentId());

    }
    @Test
    void verifyPayment_WhenGatewayThrowsException_ShouldNotSavePaymentId(){
        Payment payment = getPayment(PaymentStatus.PENDING);
        PaymentVerifyRequest request = new PaymentVerifyRequest("ord_XXXXX", "payment_XXXXX", "sign_XXXX");
        when(gateway.verifySignature(anyString(), anyString(), anyString())).thenThrow(PaymentException.class);
        assertThrows(PaymentException.class,()->razorpayService.verifyPayment(payment, request));
        assertNull(payment.getGatewayPaymentId());
    }
    private Payment getPayment(PaymentStatus status){
        return Payment.builder()
                .paymentId("paymentId-XXXX")
                .paymentMethod(PaymentMethod.ONLINE)
                .paymentStatus(status)
                .build();
    }
}