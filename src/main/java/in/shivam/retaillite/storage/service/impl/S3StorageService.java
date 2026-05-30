package in.shivam.retaillite.storage.service.impl;

import in.shivam.retaillite.storage.config.AwsProperties;
import in.shivam.retaillite.storage.service.StorageService;
import in.shivam.retaillite.storage.validation.StorageFileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service("s3StorageService")
@Primary
public class S3StorageService implements StorageService {
    private final AwsProperties awsProperties;
    private final S3Client s3Client;
    private final StorageFileValidator storageFileValidator;
    @Override
    public String upload(MultipartFile file, String folder){
        try {
            storageFileValidator.validate(file);
            String fileName= UUID.randomUUID().toString()+"_"+file.getOriginalFilename();
            String key= folder+"/"+fileName;
            PutObjectRequest request= PutObjectRequest.builder()
                    .bucket(awsProperties.bucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request,
                    RequestBody.fromBytes(file.getBytes())
            );
            return key;
        } catch (IOException e) {
            throw new RuntimeException("File upload failed",e);
        }
    }
    
    @Override
    public void delete(String key){
        DeleteObjectRequest request= DeleteObjectRequest.builder()
                .bucket(awsProperties.bucketName())
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    @Override
    public String getKey(String fileUrl) {
        //todo: extract key form the file url
        return "";
    }

    @Override
    public String getFileUrl(String key){
        return "https://"
                + awsProperties.bucketName()
                + ".s3"
                +"amazonaws.com/"
                +key;
    }
}
