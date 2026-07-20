package in.shivam.retaillite.payment.application;

import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.exception.QuantityOutOfBoundException;
import in.shivam.retaillite.inventory.service.InventoryService;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.repository.InvoiceRepository;
import in.shivam.retaillite.payment.PaymentRepository;
import in.shivam.retaillite.payment.config.RazorpayProperties;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.domain.validation.InvoiceValidation;
import in.shivam.retaillite.payment.dto.request.PaymentRequest;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import in.shivam.retaillite.payment.dto.request.RefundRequest;
import in.shivam.retaillite.payment.dto.response.CashPaymentResponse;
import in.shivam.retaillite.payment.dto.response.PaymentResponse;
import in.shivam.retaillite.payment.dto.response.RazorpayPaymentResponse;
import in.shivam.retaillite.payment.exception.*;
import in.shivam.retaillite.payment.factory.PaymentServiceFactory;
import in.shivam.retaillite.payment.mapper.PaymentMapper;
import in.shivam.retaillite.payment.response.PaymentResponseFactory;
import in.shivam.retaillite.payment.service.PaymentService;
import in.shivam.retaillite.payment.service.impl.CashService;
import in.shivam.retaillite.product.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class PaymentOrchestratorTest {

    @Mock private  PaymentRepository paymentRepository;
    @Mock private  PaymentServiceFactory paymentServiceFactory;
    @Mock private  InvoiceRepository invoiceRepository;
    @Mock private  InventoryService inventoryService;
    @Mock private  InvoiceValidation invoiceValidation;
    private PaymentOrchestrator paymentOrchestrator;
    @BeforeEach
    void setUp(){
        PaymentResponseFactory paymentResponseFactory = new PaymentResponseFactory(new RazorpayProperties("razorpay-key-id", "razorpay-key-secret", 2L, "webhook-secret"));
        PaymentMapper paymentMapper = new PaymentMapper();
        paymentOrchestrator=new PaymentOrchestrator(paymentRepository,
                paymentServiceFactory,
                paymentResponseFactory,
                invoiceRepository,
                inventoryService,
                paymentMapper,
                invoiceValidation
        );
    }

    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//
    //              PROCESS PAYMENT TEST                      //
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//

    // Immediate payment
    @Test
    void shouldCompleteImmediatePayment_WhenNoPendingPaymentExists(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.CASH
        );


        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        when(paymentRepository.findPendingPaymentByInvoiceId(anyString())).thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.pay(any(Payment.class))).thenAnswer((invocation -> {
            Payment payment= invocation.getArgument(0);
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            return payment;})
        );
        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        paymentOrchestrator.processInvoicePayment(request);
        assertEquals(InvoiceStatus.PAID, invoice.getInvoiceStatus());
        verify(inventoryService).deductStock(any(Product.class),any(Integer.class));
    }

    @Test
    void shouldRefundPayment_WhenStockDeductionFails(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.CASH
        );


        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        when(paymentRepository.findPendingPaymentByInvoiceId(anyString())).thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.pay(any(Payment.class))).thenAnswer((invocation -> {
            Payment payment= invocation.getArgument(0);
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            return payment;})
        );
        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        doThrow(QuantityOutOfBoundException.class).when(inventoryService).deductStock(any(Product.class),any(Integer.class));

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment=invocation.getArgument(0);
            payment.markRefunded();
            return payment;
        });


        paymentOrchestrator.processInvoicePayment(request);

        assertEquals(InvoiceStatus.CANCELED, invoice.getInvoiceStatus());
        verify(inventoryService).deductStock(any(Product.class),any(Integer.class));

    }
    @Test
    void shouldThrowPaymentException_WhenRefundFails(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.CASH
        );


        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        when(paymentRepository.findPendingPaymentByInvoiceId(anyString())).thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.pay(any(Payment.class))).thenAnswer((invocation -> {
            Payment payment= invocation.getArgument(0);
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            return payment;})
        );
        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        doThrow(QuantityOutOfBoundException.class).when(inventoryService).deductStock(any(Product.class),any(Integer.class));

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));


        assertThrows(PaymentException.class,()->paymentOrchestrator.processInvoicePayment(request));

        verify(inventoryService).deductStock(any(Product.class),any(Integer.class));

    }

    // Gateway payment
    @Test
    void shouldReturnPendingResponse_WhenGatewayOrderIsCreated(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.ONLINE
        );

        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);

        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        doNothing().when(inventoryService).validate(any(Product.class),any(Integer.class));
        when(paymentRepository.findPendingPaymentByInvoiceId(anyString())).thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.pay(any(Payment.class))).thenAnswer((invocation -> {
            Payment invocationArgument= invocation.getArgument(0);
            invocationArgument.setGatewayOrderId("razorpay_order_id_XXXXX");
            return invocationArgument;})
        );
        Product product=mock(Product.class);

        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);

        RazorpayPaymentResponse response = assertInstanceOf(
                RazorpayPaymentResponse.class,
                paymentOrchestrator.processInvoicePayment(request)
        );

        assertEquals("razorpay_order_id_XXXXX", response.gatewayOrderId());

        assertEquals(InvoiceStatus.PENDING, invoice.getInvoiceStatus());
    }

    @Test
    void shouldReusePendingPayment_WhenPaymentMethodMatches(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.ONLINE
        );
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);

        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);

        payment.setInvoice(invoice);
        payment.setGatewayOrderId("razorpay_order_id_XXXXX");
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        when(paymentRepository.findPendingPaymentByInvoiceId(anyString())).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.pay(any(Payment.class))).thenAnswer((invocation -> invocation.getArgument(0)));
        Product product=mock(Product.class);

        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);

        RazorpayPaymentResponse response = assertInstanceOf(
                RazorpayPaymentResponse.class,
                paymentOrchestrator.processInvoicePayment(request)
        );

        assertEquals("razorpay_order_id_XXXXX", response.gatewayOrderId());

        assertEquals(InvoiceStatus.PENDING, invoice.getInvoiceStatus());
    }

    @Test
    void shouldExpireExistingPendingPayment_WhenPaymentMethodChanges(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.CASH
        );
        Payment pendingPayment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);

        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);

        pendingPayment.setInvoice(invoice);
        pendingPayment.setGatewayOrderId("razorpay_order_id_XXXXX");
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        when(paymentRepository.findPendingPaymentByInvoiceId(anyString())).thenReturn(Optional.of(pendingPayment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.pay(any(Payment.class))).thenAnswer((invocation -> {
            Payment payment=invocation.getArgument(0);
            payment.markSuccess();
            return payment;
        }));
        Product product=mock(Product.class);

        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);

        CashPaymentResponse response = assertInstanceOf(
                CashPaymentResponse.class,
                paymentOrchestrator.processInvoicePayment(request)
        );

        assertEquals(PaymentStatus.SUCCESS, response.paymentStatus());

        assertEquals(PaymentStatus.EXPIRED,pendingPayment.getPaymentStatus());

        assertEquals(InvoiceStatus.PAID, invoice.getInvoiceStatus());
    }

    // Validation
    @Test
    void shouldThrowResourceNotFoundException_WhenInvoiceDoesNotExist(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.CASH
        );


        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.empty());
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->paymentOrchestrator.processInvoicePayment(request));
        assertEquals("Invoice not found with invoice Id: " + request.invoiceId(),exception.getMessage());

    }

    @Test
    void shouldThrowInvoiceAlreadyPaidException_WhenInvoiceIsAlreadyPaid(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.CASH
        );


        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));
        doThrow(new InvoiceAlreadyPaidException("Invoice is already paid....")).when(invoiceValidation).validateCanPay(any(Invoice.class));

        InvoiceAlreadyPaidException exception=assertThrows(InvoiceAlreadyPaidException.class,()->paymentOrchestrator.processInvoicePayment(request));
        assertEquals("Invoice is already paid....",exception.getMessage());
    }
    @Test
    void shouldThrowInvoiceInvoiceCanceledException_WhenInvoiceIsCanceled(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.CASH
        );


        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));
        doThrow(new InvoiceCanceledException("This Invoice is canceled...")).when(invoiceValidation).validateCanPay(any(Invoice.class));

        InvoiceCanceledException exception=assertThrows(InvoiceCanceledException.class,()->paymentOrchestrator.processInvoicePayment(request));
        assertEquals("This Invoice is canceled...",exception.getMessage());
    }
    @Test
    void shouldThrowQuantityOutOfBoundException_WhenStockValidationFails(){
        PaymentRequest request=new PaymentRequest(
                "INV-XXX",
                PaymentMethod.CASH
        );


        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PENDING);
        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));
        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        when(invoice.getInvoiceItems().getFirst().getQuantity()).thenReturn(10);
        doThrow(QuantityOutOfBoundException.class).when(inventoryService).validate(any(Product.class),any(Integer.class));
        assertThrows(QuantityOutOfBoundException.class,()->paymentOrchestrator.processInvoicePayment(request));
    }







    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//
    //              PROCESS INVOICE REFUND TEST               //
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//


    @Test
    void shouldProcessRefund_WhenPaymentIsSUCCESSAndCanBeRefunded(){

        RefundRequest request=new RefundRequest("INV-XXX");
        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PAID);

        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        Payment payment=getPayment(PaymentMethod.CASH,PaymentStatus.SUCCESS);
        when(paymentRepository.findByInvoice_invoiceIdAndPaymentStatus(invoice.getInvoiceId(),PaymentStatus.SUCCESS)).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment=invocation.getArgument(0);
            invocatedPayment.markRefunded();
            return invocatedPayment;
        });

        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        when(invoice.getInvoiceItems().getFirst().getQuantity()).thenReturn(1);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentOrchestrator.processInvoiceRefund(request);
        assertEquals(InvoiceStatus.CANCELED,invoice.getInvoiceStatus());
        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
    }

    @ParameterizedTest
    @CsvSource(value = {"PENDING","CANCELED"})
    void shouldThrowInvoiceNotPaidException_WhenInvoiceIsUnpaid(InvoiceStatus invoiceStatus){
        RefundRequest request=new RefundRequest("INV-XXX");
        Invoice invoice=getInvoice("INV-XXX",invoiceStatus);

        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        doThrow(new InvoiceNotPaidException("invoice is not paid. Refund is Not possible")).when(invoiceValidation).validateCanRefund(invoice);

        InvoiceNotPaidException exception=assertThrows(InvoiceNotPaidException.class,()->paymentOrchestrator.processInvoiceRefund(request));
        assertEquals("invoice is not paid. Refund is Not possible",exception.getMessage());
    }

    @Test
    void shouldThrowResourceNotFound_WhenSUCCESSPaymentNotFound() {

        RefundRequest request = new RefundRequest("INV-XXX");
        Invoice invoice = getInvoice("INV-XXX", InvoiceStatus.PAID);

        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        when(paymentRepository.findByInvoice_invoiceIdAndPaymentStatus(invoice.getInvoiceId(), PaymentStatus.SUCCESS)).thenReturn(Optional.empty());
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->paymentOrchestrator.processInvoiceRefund(request));
        assertEquals("Success Payment not found!!!",exception.getMessage());
    }
    @Test
    void shouldNotAddStock_WhenPaymentIsNotRefunded(){

        RefundRequest request=new RefundRequest("INV-XXX");
        Invoice invoice=getInvoice("INV-XXX",InvoiceStatus.PAID);

        when(invoiceRepository.findByInvoiceId(anyString())).thenReturn(Optional.of(invoice));

        Payment payment=getPayment(PaymentMethod.CASH,PaymentStatus.SUCCESS);
        when(paymentRepository.findByInvoice_invoiceIdAndPaymentStatus(invoice.getInvoiceId(),PaymentStatus.SUCCESS)).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentOrchestrator.processInvoiceRefund(request);
        assertEquals(InvoiceStatus.PAID,invoice.getInvoiceStatus());
        assertEquals(PaymentStatus.SUCCESS,payment.getPaymentStatus());
    }




    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//
    //              VERIFY PAYMENT TEST                       //
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//


    @Test
    void shouldVerifyPayment_WhenPaymentVerificationRequestIsValid(){

        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");
        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PENDING);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);
        when(paymentService.verifyPayment(any(Payment.class),any(PaymentVerifyRequest.class))).thenAnswer( invocation ->{
            Payment invocatedPayment=invocation.getArgument(0);
            invocatedPayment.setGatewayPaymentId(request.gatewayPaymentId());
            return invocatedPayment;
        });

        Product product=mock(Product.class);

        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        when(invoice.getInvoiceItems().getFirst().getQuantity()).thenReturn(2);

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentOrchestrator.verifyPayment(request);
        assertEquals(PaymentStatus.SUCCESS,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PAID,invoice.getInvoiceStatus());
    }
    @Test
    void shouldReturnPaymentResponse_WhenPaymentIsSuccessByAnotherThread(){

        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");
        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PAID);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.SUCCESS);
        payment.setInvoice(invoice);

        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        paymentOrchestrator.verifyPayment(request);
        assertEquals(PaymentStatus.SUCCESS,payment.getPaymentStatus());
        verifyNoInteractions(paymentServiceFactory);
    }
    @Test
    void shouldThrowPaymentException_WhenPaymentCannotBeVarified(){

        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");
        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.CANCELED);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.REFUNDED);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        PaymentException exception=assertThrows(PaymentException.class,()->paymentOrchestrator.verifyPayment(request));
        assertEquals("payment is Refunded...",exception.getMessage());
        assertEquals(HttpStatus.CONFLICT,exception.getStatusCode());

    }
    @Test
    void shouldThrowResourceNotFoundException_WhenNoPaymentFoundForGatewayOrderId(){

        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.empty());

        PaymentException exception=assertThrows(PaymentException.class,()->paymentOrchestrator.verifyPayment(request));
    }

    @Test
    void shouldThrowPaymentVerificationException_WhenVerificationFailed(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");
        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PENDING);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);
        when(paymentService.verifyPayment(any(Payment.class),any(PaymentVerifyRequest.class))).thenThrow(new PaymentVerificationException("payment varification failed.."));

        PaymentVerificationException exception=assertThrows(PaymentVerificationException.class,()->paymentOrchestrator.verifyPayment(request));
        assertEquals("payment varification failed..",exception.getMessage());
    }

    @Test
    @DisplayName("Refund amount because invoice is paid by another thread during current payment process")
    void shouldNotVerify_WhenThrowInvoiceAlreadyPaidException(){

        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PAID);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);
        when(paymentService.verifyPayment(any(Payment.class),any(PaymentVerifyRequest.class))).thenAnswer( invocation ->{
            Payment invocatedPayment=invocation.getArgument(0);
            invocatedPayment.setGatewayPaymentId(request.gatewayPaymentId());
            return invocatedPayment;
        });

        doThrow(InvoiceAlreadyPaidException.class).when(invoiceValidation).validateCanCompletePayment(invoice);

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment= invocation.getArgument(0);
            invocatedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
            return invocatedPayment;
        });

        paymentOrchestrator.verifyPayment(request);
        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PAID,invoice.getInvoiceStatus());
    }
    @Test
    @DisplayName(
    "Refund amount because invoice is CANCELED by another thread during current payment process"
    )
    void shouldNotVerify_WhenThrowInvoiceCanceledException(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.CANCELED);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);
        when(paymentService.verifyPayment(any(Payment.class),any(PaymentVerifyRequest.class))).thenAnswer( invocation ->{
            Payment invocatedPayment=invocation.getArgument(0);
            invocatedPayment.setGatewayPaymentId(request.gatewayPaymentId());
            return invocatedPayment;
        });

        doThrow(InvoiceCanceledException.class).when(invoiceValidation).validateCanCompletePayment(invoice);

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment= invocation.getArgument(0);
            invocatedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
            return invocatedPayment;
        });

        paymentOrchestrator.verifyPayment(request);
        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.CANCELED,invoice.getInvoiceStatus());
    }

    @Test
    void shouldNotVerify_WhenThrowResourceNotFoundException(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PENDING);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);
        when(paymentService.verifyPayment(any(Payment.class),any(PaymentVerifyRequest.class))).thenAnswer( invocation ->{
            Payment invocatedPayment=invocation.getArgument(0);
            invocatedPayment.setGatewayPaymentId(request.gatewayPaymentId());
            return invocatedPayment;
        });

        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        when(invoice.getInvoiceItems().getFirst().getQuantity()).thenReturn(2);
        doThrow(ResourceNotFoundException.class).when(inventoryService).deductStock(any(Product.class),any(Integer.class));

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment= invocation.getArgument(0);
            invocatedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
            return invocatedPayment;
        });

        paymentOrchestrator.verifyPayment(request);
        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PENDING,invoice.getInvoiceStatus());
    }

    @Test
    void shouldNotVerify_WhenDeductStockThrowsQuantityOutOfBoundException(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PENDING);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);
        when(paymentService.verifyPayment(any(Payment.class),any(PaymentVerifyRequest.class))).thenAnswer( invocation ->{
            Payment invocatedPayment=invocation.getArgument(0);
            invocatedPayment.setGatewayPaymentId(request.gatewayPaymentId());
            return invocatedPayment;
        });

        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        when(invoice.getInvoiceItems().getFirst().getQuantity()).thenReturn(2);
        doThrow(new QuantityOutOfBoundException("Quantity is not available right now!!")).when(inventoryService).deductStock(any(Product.class),any(Integer.class));

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment= invocation.getArgument(0);
            invocatedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
            return invocatedPayment;
        });

        paymentOrchestrator.verifyPayment(request);
        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PENDING,invoice.getInvoiceStatus());
    }



    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//
    //              VERIFY WEBHOOK TEST                       //
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//

    @Test
    void shouldVerifyWebHook(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PENDING);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        assertDoesNotThrow(()->paymentOrchestrator.verifyWebhook(request.gatewayPaymentId(),request.gatewayOrderId()));
        assertEquals(PaymentStatus.SUCCESS,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PAID,invoice.getInvoiceStatus());
        verify(inventoryService).deductStock(any(),any());
    }
    @Test
    void shouldNotContinueVerifyWebHook_WhenPaymentIsSuccess(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PAID);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.SUCCESS);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        paymentOrchestrator.verifyWebhook(request.gatewayPaymentId(),request.gatewayOrderId());
        assertEquals(PaymentStatus.SUCCESS,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PAID,invoice.getInvoiceStatus());
        verifyNoInteractions(inventoryService);
    }

    @Test
    void shouldThrowInvoiceAlreadyPaidException_WhenInvoiceIsPAIDDuringPaymentProcess(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        //this invoice is paid by another thread while older payment still in process
        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PAID);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        doThrow(InvoiceAlreadyPaidException.class).when(invoiceValidation).validateCanCompletePayment(invoice);

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment= invocation.getArgument(0);
            invocatedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
            return invocatedPayment;
        });

        paymentOrchestrator.verifyWebhook(request.gatewayPaymentId(),request.gatewayOrderId());

        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PAID,invoice.getInvoiceStatus());

    }

    @Test
    void shouldThrowInvoiceCanceledException_WhenInvoiceIsCANCLEDDuringPaymentProcess(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        //this invoice is CANCELED by another thread while older payment still in process
        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.CANCELED);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));

        doThrow(InvoiceCanceledException.class).when(invoiceValidation).validateCanCompletePayment(invoice);

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment= invocation.getArgument(0);
            invocatedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
            return invocatedPayment;
        });

        paymentOrchestrator.verifyWebhook(request.gatewayPaymentId(),request.gatewayOrderId());

        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.CANCELED,invoice.getInvoiceStatus());

    }
    @Test
    void shouldThrowResourceNotFoundException_WhenProductInventoryNotFound(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        //this invoice is paid by another thread while older payment still in process
        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PAID);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));
        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        when(invoice.getInvoiceItems().getFirst().getQuantity()).thenReturn(2);
        doThrow(ResourceNotFoundException.class).when(inventoryService).deductStock(any(Product.class),any(Integer.class));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment= invocation.getArgument(0);
            invocatedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
            return invocatedPayment;
        });

        paymentOrchestrator.verifyWebhook(request.gatewayPaymentId(),request.gatewayOrderId());

        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PAID,invoice.getInvoiceStatus());

    }


    @Test
    void shouldThrowQuantityOutOfBound_WhenProductStockNotEnough(){
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id_XXXXX","razorpay_payment_id_XXXXX","razorpay_signature");

        //this invoice is paid by another thread while older payment still in process
        Invoice invoice=getInvoice("INV_XXXXX",InvoiceStatus.PAID);
        Payment payment=getPayment(PaymentMethod.ONLINE,PaymentStatus.PENDING);
        payment.setInvoice(invoice);
        when(paymentRepository.findByGatewayOrderId(anyString())).thenReturn(Optional.of(payment));
        Product product=mock(Product.class);
        when(invoice.getInvoiceItems().getFirst().getProduct()).thenReturn(product);
        when(invoice.getInvoiceItems().getFirst().getQuantity()).thenReturn(2);
        doThrow(QuantityOutOfBoundException.class).when(inventoryService).deductStock(any(Product.class),any(Integer.class));

        PaymentService paymentService=mock(PaymentService.class);
        when(paymentServiceFactory.getPaymentService(anyString())).thenReturn(paymentService);

        when(paymentService.refund(any(Payment.class))).thenAnswer(invocation -> {
            Payment invocatedPayment= invocation.getArgument(0);
            invocatedPayment.setPaymentStatus(PaymentStatus.REFUNDED);
            return invocatedPayment;
        });

        paymentOrchestrator.verifyWebhook(request.gatewayPaymentId(),request.gatewayOrderId());

        assertEquals(PaymentStatus.REFUNDED,payment.getPaymentStatus());
        assertEquals(InvoiceStatus.PAID,invoice.getInvoiceStatus());

    }




    private Payment getPayment(PaymentMethod paymentMethod,PaymentStatus paymentStatus) {
        return Payment.builder()
                .paymentId("payment id")
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();
    }
    private Invoice getInvoice(String invoiceId,InvoiceStatus status){

        Product product=mock(Product.class);
        InvoiceItem invoiceItem=mock(InvoiceItem.class);

        return Invoice.builder()
                .invoiceId(invoiceId)
                .customerName("customer")
                .customerNumber("0123456789")
                .customerEmail("customer@retiallite.com")
                .subTotal(BigDecimal.valueOf(100))
                .tax(BigDecimal.valueOf(10))
                .grandTotal(BigDecimal.valueOf(110))
                .invoiceStatus(status)
                .invoiceItems(List.of(invoiceItem)).build();
    }
    private InvoiceItem invoiceItem(){
        Product product= Product.builder()
                .productId("PROD-XXX")
                .name("productname")
                .price(BigDecimal.valueOf(100L))
                .taxRate(BigDecimal.valueOf(10))
                .category(Category.builder().build())
                .build();
        return InvoiceItem.builder()
                .invoiceItemId("item-xxx")
                .product(product).quantity(1).unitPrice(BigDecimal.valueOf(100L)).lineTotal(BigDecimal.valueOf(100L)).build();
    }
}