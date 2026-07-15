package in.shivam.retaillite.storage.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.ValueSources;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageFileValidatorTest {
    StorageFileValidator storageFileValidator=new StorageFileValidator();

    @ParameterizedTest
    @ValueSource(strings = {"document.pdf", "image.jpg", "photo.JPEG", "UPPERCASE.PDF"})
    void validate_WithValidFiles_ShouldPassWithoutExceptions(String filename){
        MultipartFile multipartFile=new MockMultipartFile(
                "file",
                filename,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "valid content string".getBytes()
        );
        assertDoesNotThrow(()->storageFileValidator.validate(multipartFile));
    }

    @Test
    void validate_WhenFileIsNull_ThrowsBadRequest() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                storageFileValidator.validate(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("File is required", exception.getReason());
    }

    @Test
    void validate_WhenFileIsEmpty_ThrowsBadRequest() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[0] // 0 bytes = empty
        );

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                storageFileValidator.validate(emptyFile)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("File is empty", exception.getReason());
    }

    @Test
    void validate_WhenFileExceeds8MB_ThrowsBadRequest() {
        // Arrange
        MultipartFile heavyMockFile = mock(MultipartFile.class);

        // Simulating exactly 8MB + 1 byte
        long oversized = (8L * 1024 * 1024) + 1;

        when(heavyMockFile.isEmpty()).thenReturn(false);
        when(heavyMockFile.getSize()).thenReturn(oversized);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                storageFileValidator.validate(heavyMockFile)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Maximum upload size is 8MB", exception.getReason());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void validate_WhenFileNameIsBlankOrEmpty_ThrowsBadRequest(String blankName) {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn(blankName);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                storageFileValidator.validate(mockFile)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid filename", exception.getReason());
    }

    @ParameterizedTest
    @ValueSource(strings = {"../../etc/passwd", "folder/file.jpg", "C:\\windows\\file.pdf"})
    void validate_WhenPathTraversalAttempted_ThrowsBadRequest(String maliciousName) {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn(maliciousName);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                storageFileValidator.validate(mockFile)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid filename", exception.getReason());
    }

    @ParameterizedTest
    @ValueSource(strings = {"malicious.exe", "script.sh", "archive.zip", "no-extension"})
    void validate_WhenExtensionIsBlocked_ThrowsBadRequest(String invalidName) {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                invalidName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "some content".getBytes()
        );

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                storageFileValidator.validate(invalidFile)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid file extension", exception.getReason());
    }
}
