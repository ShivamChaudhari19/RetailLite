package in.shivam.retaillite.payment.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "razorpay")
public record RazorpayProperties(
        @NotBlank(message = "Razorpay credentials KeyId: must not be blank")
        String keyId,
        @NotBlank(message = "Razorpay credentials KeySecret: must not be blank")
        String keySecret,
        @NotNull(message = "Razorpay checkout timeout must not be blank")
        @Positive
        Long timeout,
        @NotBlank(message = "Razorpay webhook Secret cannot be blank")
        String webhookSecret
) {}
