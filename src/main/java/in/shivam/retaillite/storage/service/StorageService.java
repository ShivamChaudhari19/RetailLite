package in.shivam.retaillite.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String upload(MultipartFile file, String folder);

    void delete(String key);
    String getKey(String fileUrl);
    String getFileUrl(String key);
}
