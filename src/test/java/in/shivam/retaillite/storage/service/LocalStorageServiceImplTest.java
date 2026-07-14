package in.shivam.retaillite.storage.service;

import in.shivam.retaillite.storage.service.impl.LocalStorageService;
import in.shivam.retaillite.storage.validation.StorageFileValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocalStorageServiceImplTest {

    @Mock
    private StorageFileValidator storageFileValidator;

    @InjectMocks
    private LocalStorageService storageService;

    @Test
    void shouldUploadFile(){
        MultipartFile multipartFile=new MockMultipartFile(
                "testFile",
                "name.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image content string".getBytes()
        );
        String folder="testFolder";
        try(MockedStatic<Files> mockedFiles=mockStatic(Files.class)){
            mockedFiles.when(()->Files.createDirectories(any(Path.class))).thenReturn(null);
            mockedFiles.when(()->Files.copy(any(InputStream.class),any(Path.class),any(StandardCopyOption.class))).thenReturn(0L);
            String key=storageService.upload(multipartFile,folder);
            assertNotNull(key);
            assertTrue(key.startsWith(folder + "/"), "Key should contain the folder prefix");
            assertTrue(key.endsWith("_" + multipartFile.getOriginalFilename()), "Key should end with the original filename");
        }
    }

    @Test
    void shouldThrowException_WhenFileNotSave() throws IOException {
        MultipartFile multipartFile=mock(MultipartFile.class);
        String folder="testFolder";

        doNothing().when(storageFileValidator).validate(any(MultipartFile.class));
        when(multipartFile.getOriginalFilename()).thenReturn("file name.jpg");
        when(multipartFile.getInputStream()).thenThrow(IOException.class);
        assertThrows(ResponseStatusException.class,()->storageService.upload(multipartFile,folder));
    }

    @Test
    void delete_WhenFileExists_DeletesFileSuccessfully() {

        String key = "testFolder/uuid_name.jpg";

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);
            assertDoesNotThrow(() -> storageService.delete(key));
        }
    }

    @Test
    void delete_WhenFileSystemThrowsIOException_ThrowsInternalServerError() {

        String key = "testFolder/uuid_name.jpg";


        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class)))
                    .thenThrow(new IOException("Permission denied"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                    storageService.delete(key)
            );

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
            assertEquals("Failed to delete  file", exception.getReason());
        }
    }

    @Test
    void getKey_WhenValidUrlPassed_ReturnsStrippedKey() {

        String fileUrl = "/uploads/testFolder/uuid_name.jpg";
        String expectedKey = "testFolder/uuid_name.jpg";

        String actualKey = storageService.getKey(fileUrl);

        assertEquals(expectedKey, actualKey);
    }

    @Test
    void getFileUrl_WhenValidKeyPassed_ReturnsFormattedUrl() {

        String key = "testFolder/uuid_name.jpg";
        String expectedUrl = "/uploads/testFolder/uuid_name.jpg";

        String actualUrl = storageService.getFileUrl(key);

        assertEquals(expectedUrl, actualUrl);
    }
}
