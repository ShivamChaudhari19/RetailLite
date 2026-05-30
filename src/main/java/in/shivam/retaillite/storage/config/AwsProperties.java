package in.shivam.retaillite.storage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
        String accessKey,
        String secretKey,
        String region,
        String bucketName,
        String endpoint
) {
}
