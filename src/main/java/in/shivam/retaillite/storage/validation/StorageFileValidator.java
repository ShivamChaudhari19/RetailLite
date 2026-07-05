package in.shivam.retaillite.storage.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
@Slf4j
@Component
public class StorageFileValidator {
    private static final long MAX_UPLOAD_SIZE_IN_BYTES = 8L * 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSION=List.of(
            "jpg",
            "jpeg",
            "pdf"
    );
    public void validate(MultipartFile file) {
        log.debug("starting storage file validation");

        validateNull(file);
        validateEmpty(file);
        validateFileSize(file);
        validateFileName(file);
        validateBlockedExtension(file);

        log.debug(
                "storage validation completed successfully for file:{}",
                file.getOriginalFilename()
        );
    }

    private void validateNull(MultipartFile file) {
        if (file == null) {
            log.warn("storage validation failed:file is null");
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "File is required"
            );
        }
    }

    private void validateEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            log.warn("storage validation failed:file is empty");
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "File is empty"
            );
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_UPLOAD_SIZE_IN_BYTES) {
            log.warn(
                    "storage validation failed:file too large,size:{}",
                    file.getSize()
            );
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Maximum upload size is 8MB"
            );
        }
    }

    private void validateFileName(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            log.warn("storage validation failed:invalid filename");
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Invalid filename"
            );
        }

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            log.warn(
                    "storage validation failed:path traversal attempt:{}",
                    fileName
            );
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Invalid filename"
            );
        }
    }

    private void validateBlockedExtension(MultipartFile file) {
        String fileName = Objects.requireNonNull(
                file.getOriginalFilename()
        ).toLowerCase();
        boolean blocked = BLOCKED_EXTENSION.stream()
                .anyMatch(fileName::endsWith);

        if (blocked) {
            log.warn(
                    "storage validation failed:blocked extension:{}",
                    fileName
            );
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Invalid file extension"
            );
        }
    }

    /*
     * TODO:
     *  Add magic-byte signature validation for stronger
     *  malicious file detection.
     */
}
