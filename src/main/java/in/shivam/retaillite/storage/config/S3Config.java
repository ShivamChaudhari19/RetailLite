package in.shivam.retaillite.storage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class S3Config {
    private final AwsProperties awsProperties;
    @Bean
    public S3Client s3Client(){
        AwsBasicCredentials credentials=
                AwsBasicCredentials.create(
                        awsProperties.accessKey(),
                        awsProperties.secretKey()
                );
        return S3Client.builder()
                . endpointOverride(URI.create(awsProperties.endpoint()))
                .region(Region.of(awsProperties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

//    @Bean
//    public S3Presigner s3Presigner(){
//        AwsBasicCredentials credentials=
//                AwsBasicCredentials.create(
//                        awsProperties.accessKey(),
//                        awsProperties.secretKey()
//                );
//        return S3Presigner.builder()
//                .region(Region.of(awsProperties.region()))
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        credentials
//                ))
//                .build();
//    }
}
