package in.shivam.retaillite.payment.controller;

import in.shivam.retaillite.payment.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class RazorpayWebhookController {
    private final WebhookService webhookService;

//    url becomes http://localhost:8080/api/v1.0/webhook/payment/verify
    @PostMapping("/razorpay")
    public ResponseEntity<Void> verifyPayment(
            @RequestBody
            String payload,
            @RequestHeader("X-Razorpay-Signature")
            String signature
    ){

        webhookService.process(payload,signature);
        return ResponseEntity.ok().build();
    }
}
