package in.shivam.retaillite.payment.service;

import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import in.shivam.retaillite.payment.application.PaymentOrchestrator;
import in.shivam.retaillite.payment.config.RazorpayProperties;
import in.shivam.retaillite.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    private final PaymentOrchestrator paymentOrchestrator;
    private final RazorpayProperties razorpayProperties;
    private final PaymentQueryService paymentQueryService;

    public void process(String payload,String signature){
        String orderId = null;
        String paymentId;
        try {
            Utils.verifyWebhookSignature(payload,signature,razorpayProperties.webhookSecret());
            JSONObject event=new JSONObject(payload);
            String eventType = event.getString("event");
            if (!"payment.captured".equals(eventType)){
                return;
            }
            JSONObject payment=event.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");
            paymentId=payment.getString("id");
            orderId=payment.getString("order_id");
            paymentOrchestrator.verifyWebhook(paymentId,orderId);
        } catch (RazorpayException e) {
            throw new PaymentException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(ObjectOptimisticLockingFailureException e){
            if(paymentQueryService.isAlreadySuccessful(orderId)){
                log.info("Duplicate webhook ignored.");
                return;
            }
            throw e;
        }
    }
}
