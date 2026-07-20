package in.shivam.retaillite.payment.service;

import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import in.shivam.retaillite.payment.application.PaymentOrchestrator;
import in.shivam.retaillite.payment.config.RazorpayProperties;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.exception.PaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

        @Mock
        private PaymentOrchestrator paymentOrchestrator;

        @Mock
        private RazorpayProperties razorpayProperties;

        @Mock
        private PaymentQueryService paymentQueryService;

        @InjectMocks
        private WebhookService webhookService;

        private static final String SIGNATURE = "valid-signature";

        @BeforeEach
        void setUp() {
            when(razorpayProperties.webhookSecret())
                    .thenReturn("webhook-secret");
        }

        private String paymentCapturedPayload() {
            return """
                {
                  "event":"payment.captured",
                  "payload":{
                    "payment":{
                      "entity":{
                        "id":"pay_123",
                        "order_id":"order_123"
                      }
                    }
                  }
                }
                """;
        }
    @Test
    void shouldVerifyWebhook_WhenPaymentCapturedEventReceived() throws Exception {

        String payload = paymentCapturedPayload();

        try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {

            webhookService.process(payload, SIGNATURE);

            mocked.verify(() ->
                    Utils.verifyWebhookSignature(
                            payload,
                            SIGNATURE,
                            "webhook-secret"
                    ));

            verify(paymentOrchestrator)
                    .verifyWebhook("pay_123", "order_123");

            verifyNoInteractions(paymentQueryService);
        }
    }
    @Test
    void shouldIgnoreWebhook_WhenEventIsNotPaymentCaptured() throws Exception {

        String payload = """
            {
              "event":"payment.failed"
            }
            """;

        try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {

            webhookService.process(payload, SIGNATURE);

            mocked.verify(() ->
                    Utils.verifyWebhookSignature(
                            payload,
                            SIGNATURE,
                            "webhook-secret"
                    ));

            verifyNoInteractions(paymentOrchestrator);
            verifyNoInteractions(paymentQueryService);
        }
    }

    @Test
    void shouldThrowPaymentException_WhenSignatureVerificationFails() throws Exception {

        String payload = paymentCapturedPayload();

        try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {

            mocked.when(() ->
                            Utils.verifyWebhookSignature(
                                    anyString(),
                                    anyString(),
                                    anyString()))
                    .thenThrow(new RazorpayException("Invalid Signature"));

            assertThrows(
                    PaymentException.class,
                    () -> webhookService.process(payload, SIGNATURE)
            );

            verifyNoInteractions(paymentOrchestrator);
        }
    }




    @Test
    void shouldIgnoreDuplicateWebhook_WhenAlreadyProcessed() throws Exception {

        String payload = paymentCapturedPayload();

        try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {

            doThrow(new ObjectOptimisticLockingFailureException(
                    Payment.class,
                    1L
            )).when(paymentOrchestrator)
                    .verifyWebhook(anyString(), anyString());

            when(paymentQueryService.isAlreadySuccessful("order_123"))
                    .thenReturn(true);

            assertDoesNotThrow(() ->
                    webhookService.process(payload, SIGNATURE));

            verify(paymentQueryService)
                    .isAlreadySuccessful("order_123");
        }
    }

    @Test
    void shouldRethrowOptimisticLockException_WhenPaymentNotAlreadySuccessful() throws Exception {

        String payload = paymentCapturedPayload();

        try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {

            doThrow(new ObjectOptimisticLockingFailureException(
                    Payment.class,
                    1L
            )).when(paymentOrchestrator)
                    .verifyWebhook(anyString(), anyString());

            when(paymentQueryService.isAlreadySuccessful("order_123"))
                    .thenReturn(false);

            assertThrows(
                    ObjectOptimisticLockingFailureException.class,
                    () -> webhookService.process(payload, SIGNATURE)
            );
        }
    }
    @Test
    void shouldThrowJSONException_WhenPayloadIsMalformed() throws Exception {

        String payload = "{ invalid json }";

        try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {

            assertThrows(
                    org.json.JSONException.class,
                    () -> webhookService.process(payload, SIGNATURE)
            );

            verifyNoInteractions(paymentOrchestrator);
        }
    }
}