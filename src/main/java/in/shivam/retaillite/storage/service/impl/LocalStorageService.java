package in.shivam.retaillite.storage.service.impl;

import in.shivam.retaillite.storage.service.StorageService;
import in.shivam.retaillite.storage.validation.StorageFileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service("localStorageService")
public class LocalStorageService implements StorageService {
    private static final String BASE_UPLOAD_DIR = "uploads";
    private final StorageFileValidator storageFileValidator;
    @Override
    public String upload(MultipartFile file, String folder) {
        storageFileValidator.validate(file);
        try {
            String fileName= UUID.randomUUID().toString()+"_"+file.getOriginalFilename();
            String key= folder+"/"+fileName;
            Path path= Paths.get(BASE_UPLOAD_DIR,folder).toAbsolutePath().normalize();
            Files.createDirectories(path);
            Path targetLocation= path.resolve(fileName);
            Files.copy(file.getInputStream(),targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("file: {} uploaded successfully on the: {}",
                    fileName,folder);
            return key;
        } catch (IOException e) {
            log.debug("local file upload failed",e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "failed to upload file");
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path path = Paths.get(BASE_UPLOAD_DIR, key);
            Files.deleteIfExists(path);
            log.info("file deleted successfully: {}",key);
        } catch (IOException e) {
            log.debug("failed to delete file:{}",key);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete  file");
        }
    }

    @Override
    public String getKey(String fileUrl) {
        return fileUrl.substring("/uploads/".length());
    }

    @Override
    public String getFileUrl(String key) {
        return "/uploads/"+key;
    }
}
