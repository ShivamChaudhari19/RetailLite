package in.shivam.retaillite.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.shivam.retaillite.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentConfig {
    private final RazorpayProperties razorpayProperties;
    @Bean
    public RazorpayClient razorpayClient() {
        try {
            String key=razorpayProperties.keyId();
            String secret=razorpayProperties.keySecret();
            return new RazorpayClient(key,secret);
        } catch (RazorpayException e) {
            log.error("razorpay client failed to instantiate due to invalid credentials or network: ",e);
            throw new PaymentException("razorpay client failed to instantiate: "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
