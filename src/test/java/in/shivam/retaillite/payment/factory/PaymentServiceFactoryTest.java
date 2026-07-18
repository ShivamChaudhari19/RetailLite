package in.shivam.retaillite.payment.factory;

import in.shivam.retaillite.payment.exception.PaymentException;
import in.shivam.retaillite.payment.service.PaymentService;
import in.shivam.retaillite.payment.service.impl.CashService;
import in.shivam.retaillite.payment.service.impl.RazorpayService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentServiceFactoryTest {
    static PaymentServiceFactory paymentServiceFactory;

    @BeforeAll
    static void init(){
        CashService cashService=mock(CashService.class);
        when(cashService.getPaymentMethod()).thenReturn("CASH");
        RazorpayService razorpayService=mock(RazorpayService.class);
        when(razorpayService.getPaymentMethod()).thenReturn("ONLINE");
        paymentServiceFactory=new PaymentServiceFactory(List.of(cashService,razorpayService));
    }

    @Test
    void getPaymentService_WhenPaymentMethodIsCASH_ShouldReturnCASHPaymentService() {
        PaymentService paymentService=paymentServiceFactory.getPaymentService("CASH");
        assertEquals("CASH",paymentService.getPaymentMethod());
    }
    @Test
    void getPaymentService_WhenPaymentMethodIsONLINE_ShouldReturnRazorpayPaymentService() {
        PaymentService paymentService=paymentServiceFactory.getPaymentService("ONLINE");
        assertEquals("ONLINE",paymentService.getPaymentMethod());
    }
    @Test
    void getPaymentService_WhenPaymentMethodIsInvalid_ShouldThrowPaymentException() {
        PaymentException exception=assertThrows(PaymentException.class,()->paymentServiceFactory.getPaymentService("CARD"));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatusCode());
        assertEquals("Unsupported Payment Method: CARD",exception.getMessage());

    }
}