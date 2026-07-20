package in.shivam.retaillite.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.payment.application.PaymentOrchestrator;
import in.shivam.retaillite.payment.dto.request.PaymentRequest;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import in.shivam.retaillite.payment.dto.request.RefundRequest;
import in.shivam.retaillite.payment.dto.response.CashPaymentResponse;
import in.shivam.retaillite.payment.dto.response.PaymentResponse;
import in.shivam.retaillite.payment.dto.response.RazorpayPaymentResponse;
import in.shivam.retaillite.payment.dto.response.RefundResponse;
import in.shivam.retaillite.payment.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PaymentOrchestrator paymentOrchestrator;


    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//
    //          pay controller test                   //
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//

    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    void shouldReturn200_WhenPayIsSuccessfullyProceed() throws Exception {
        PaymentRequest paymentRequest=new PaymentRequest("Inv_XXXXX", PaymentMethod.CASH);
        String paymentRequestJson=objectMapper.writeValueAsString(paymentRequest);

        PaymentResponse cashPaymentResponse=mock(CashPaymentResponse.class);

        when(paymentOrchestrator.processInvoicePayment(any(PaymentRequest.class))).thenReturn((PaymentResponse) cashPaymentResponse);

        mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJson)
                        .with(csrf()))
                .andExpect(status().isCreated());
    }
    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    void shouldReturn400_WhenPaymentOrchestratorThrowsInvoiceCanceledException() throws Exception {
        PaymentRequest paymentRequest=new PaymentRequest("Inv_XXXXX", PaymentMethod.CASH);
        String paymentRequestJson=objectMapper.writeValueAsString(paymentRequest);

//        PaymentResponse cashPaymentResponse=mock(CashPaymentResponse.class);

        when(paymentOrchestrator.processInvoicePayment(any(PaymentRequest.class))).thenThrow(InvoiceCanceledException.class);

        mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    void shouldReturn409_WhenPaymentOrchestratorThrowsInvoiceAlreadyPaidException() throws Exception {
        PaymentRequest paymentRequest=new PaymentRequest("Inv_XXXXX", PaymentMethod.CASH);
        String paymentRequestJson=objectMapper.writeValueAsString(paymentRequest);

//        PaymentResponse cashPaymentResponse=mock(CashPaymentResponse.class);

        when(paymentOrchestrator.processInvoicePayment(any(PaymentRequest.class))).thenThrow(InvoiceAlreadyPaidException.class);

        mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJson)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    void shouldReturn500_WhenPaymentOrchestratorThrowsPaymentException() throws Exception {
        PaymentRequest paymentRequest=new PaymentRequest("Inv_XXXXX", PaymentMethod.CASH);
        String paymentRequestJson=objectMapper.writeValueAsString(paymentRequest);

//        PaymentResponse cashPaymentResponse=mock(CashPaymentResponse.class);

        when(paymentOrchestrator.processInvoicePayment(any(PaymentRequest.class))).thenThrow(new PaymentException("internal server error", HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequestJson)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }





    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//
    //          refund controller test                //
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturn202_WhenRefundIsAccepted() throws Exception {
        RefundRequest request=new RefundRequest("Inv_XXX");
        String requestJson=objectMapper.writeValueAsString(request);

        RefundResponse response=RefundResponse.builder().build();
        when(paymentOrchestrator.processInvoiceRefund(request)).thenReturn(response);

        mockMvc.perform(post("/payment/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isAccepted());
    }
    @Test
    @WithMockUser(roles = {"USER"})
    void shouldReturn400_WhenOrchestratorThrowsInvoiceNotPaidException() throws Exception {
        RefundRequest request=new RefundRequest("Inv_XXX");
        String requestJson=objectMapper.writeValueAsString(request);

        when(paymentOrchestrator.processInvoiceRefund(request)).thenThrow(InvoiceNotPaidException.class);

        mockMvc.perform(post("/payment/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldReturn409_WhenOrchestratorThrowsObjectOptimisticLockingFailureException() throws Exception {
        RefundRequest request=new RefundRequest("Inv_XXX");
        String requestJson=objectMapper.writeValueAsString(request);

        when(paymentOrchestrator.processInvoiceRefund(request)).thenThrow( ObjectOptimisticLockingFailureException.class);

        mockMvc.perform(post("/payment/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }


    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//
    //          verify controller test                //
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_//

    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    void shouldReturn200_WhenPaymentIsVerified() throws Exception {
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id","razorpay_payment_id","razorpay_signature");
        String requestJson=objectMapper.writeValueAsString(request);

        PaymentResponse response=mock(RazorpayPaymentResponse.class);
        when(paymentOrchestrator.verifyPayment(request)).thenReturn(response);
        mockMvc.perform(post("/payment/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    void shouldReturn500_WhenPaymentIsNotVerified() throws Exception {
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id","razorpay_payment_id","razorpay_signature");
        String requestJson=objectMapper.writeValueAsString(request);

        when(paymentOrchestrator.verifyPayment(request)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"payment verification failed"));
        mockMvc.perform(post("/payment/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    void shouldReturn400_WhenSignatureVerificationFailed() throws Exception {
        PaymentVerifyRequest request=new PaymentVerifyRequest("razorpay_order_id","razorpay_payment_id","invalid_razorpay_signature");
        String requestJson=objectMapper.writeValueAsString(request);

        when(paymentOrchestrator.verifyPayment(request)).thenThrow(PaymentVerificationException.class);
        mockMvc.perform(post("/payment/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

}