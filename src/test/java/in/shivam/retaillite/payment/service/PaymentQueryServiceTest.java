package in.shivam.retaillite.payment.service;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.payment.PaymentRepository;
import in.shivam.retaillite.payment.domain.entity.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @InjectMocks
    private PaymentQueryService paymentQueryService;

    @Test
    void isAlreadySuccessful_WhenPaymentIsSuccessForGatewayOrderId_ShouldReturnTrue() {
        Payment payment=mock(Payment.class);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.SUCCESS);

        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));
        assertTrue(paymentQueryService.isAlreadySuccessful("raz_order_id_XXXXX"));
    }

    @Test
    void isAlreadySuccessful_WhenPaymentIsNotSuccessForGatewayOrderId_ShouldReturnFalse() {
        Payment payment=mock(Payment.class);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);

        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));
        assertFalse(paymentQueryService.isAlreadySuccessful("raz_order_id_XXXXX"));
    }
    @Test
    void isAlreadySuccessful_WhenGatewayOrderIdIsNull_ShouldThrowResourceNotFoundException() {

        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->paymentQueryService.isAlreadySuccessful(null));
        assertEquals("OrderId is null ",exception.getMessage());
    }
    @Test
    void isAlreadySuccessful_WhenPaymentNotFoundForGatewayOrderId_ShouldThrowException() {

        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.empty());
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->paymentQueryService.isAlreadySuccessful("raz_order_id_XXXXX"));
        assertEquals("payment not found for orderId: raz_order_id_XXXXX",exception.getMessage());
    }
}