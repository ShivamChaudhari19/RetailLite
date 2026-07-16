package in.shivam.retaillite.common.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class ImageValidationTest {
    private final ImageValidation imageValidation = new ImageValidation();

    @Test
    void shouldValidateValidJpegImage() throws IOException {

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                outputStream.toByteArray()
        );

        assertDoesNotThrow(() -> imageValidation.validate(file));
    }
    @Test
    void shouldValidateValidPngImage() throws IOException {

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                outputStream.toByteArray()
        );

        assertDoesNotThrow(() -> imageValidation.validate(file));
    }

    @Test
    void shouldThrowException_WhenExtensionIsUnsupported() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.gif",
                MediaType.IMAGE_GIF_VALUE,
                new byte[]{1,2,3}
        );

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class,
                        () -> imageValidation.validate(file));

        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldThrowException_WhenContentTypeIsUnsupported() throws IOException {

        BufferedImage image = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image,"jpg",outputStream);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.APPLICATION_PDF_VALUE,
                outputStream.toByteArray()
        );

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class,
                        () -> imageValidation.validate(file));

        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldThrowException_WhenFileSizeExceedsLimit() {

        byte[] bytes = new byte[2 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                bytes
        );

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class,
                        () -> imageValidation.validate(file));

        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldThrowException_WhenImageIsCorrupted() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "not-an-image".getBytes()
        );

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class,
                        () -> imageValidation.validate(file));

        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }
    @Test
    void shouldThrowException_WhenImageWidthExceedsLimit() throws IOException {

        BufferedImage image =
                new BufferedImage(6000, 100, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image,"jpg",outputStream);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                outputStream.toByteArray()
        );

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class,
                        () -> imageValidation.validate(file));

        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }




    @Test
    void shouldThrowException_WhenImageHeightExceedsLimit() throws IOException {

        BufferedImage image =
                new BufferedImage(100, 6000, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image,"jpg",outputStream);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                outputStream.toByteArray()
        );

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class,
                        () -> imageValidation.validate(file));

        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldThrowResponseStatusException_WhenImageCannotBeRead() throws IOException {

        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("image.jpg");
        when(file.getSize()).thenReturn(1024L);
//        when(file.getContentType()).thenReturn(MediaType.IMAGE_JPEG_VALUE);

        when(file.getInputStream())
                .thenThrow(new IOException("Unable to read file"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> imageValidation.validate(file)
        );

        assertEquals(BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "Unable to validate image dimension",
                exception.getReason()
        );
    }
}