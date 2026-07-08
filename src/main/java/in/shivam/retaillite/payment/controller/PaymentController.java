package in.shivam.retaillite.payment.controller;

import in.shivam.retaillite.payment.dto.request.PaymentRequest;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import in.shivam.retaillite.payment.dto.response.PaymentResponse;
import in.shivam.retaillite.payment.dto.request.RefundRequest;
import in.shivam.retaillite.payment.dto.response.RefundResponse;
import in.shivam.retaillite.payment.application.PaymentOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@Validated
public class PaymentController {

    private final PaymentOrchestrator paymentOrchestrator;

    @PostMapping("/pay")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<PaymentResponse> pay(
            @RequestBody
            @Valid PaymentRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentOrchestrator.processInvoicePayment(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> refund(
            @RequestBody
            @Valid RefundRequest request
    ){
        return
                ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(paymentOrchestrator.processInvoiceRefund(request));
    }
    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<PaymentResponse> response(
            @RequestBody
            @Valid PaymentVerifyRequest request
    ){
        return ResponseEntity.ok(
                paymentOrchestrator.verifyPayment(request)
        );
    }
}
