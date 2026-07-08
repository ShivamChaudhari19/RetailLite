package in.shivam.retaillite.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
     Pending pending
) {
    public record Pending(Long paymentTimeout){}
}
