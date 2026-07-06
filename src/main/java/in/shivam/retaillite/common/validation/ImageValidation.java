package in.shivam.retaillite.common.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
public class ImageValidation {
    private static final long MAX_IMAGE_SIZE_IN_BYTES = 2L*1024*1024;
    private static final int MAX_IMAGE_WIDTH = 5000;

    private static final int MAX_IMAGE_HEIGHT = 5000;
    private static final Set<String> ALLOWED_CONTENT_TYPES= Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "content/jpg"
    );
    private static final Set<String> ALLOWED_IMAGE_EXTENSION=Set.of(
            "png",
            "jpeg",
            "jpg"
    );
    public void validate(MultipartFile file){
        validateSize(file);
        validateExtension(file);
        BufferedImage image=extractImage(file);
        validateImageContent(image);
        validateImageDimension(image);
        validateContentType(file);

    }

    private void validateExtension(MultipartFile file) {
        String fileNameExtension= Objects.requireNonNull(file.getOriginalFilename())
                .toLowerCase()
                .substring(file.getOriginalFilename().toLowerCase().lastIndexOf(".")+1);
        if (!ALLOWED_IMAGE_EXTENSION.contains(fileNameExtension)){
            log.warn("file validation failed: not valid file");
            throw new ResponseStatusException(BAD_REQUEST,
                    "Image not supported: jpeg,jpg only supported files");
        }
    }

    private void validateSize(MultipartFile file){
        if (file.getSize()>MAX_IMAGE_SIZE_IN_BYTES){
            log.warn("file validation failed: file size is large");
            throw new ResponseStatusException(BAD_REQUEST,"file size is should be less than 2Mb");
        }
    }
    private void validateContentType(MultipartFile file) {

        String contentType = file.getContentType();

        if (
                contentType == null
                        || !ALLOWED_CONTENT_TYPES.contains(contentType)
        ) {

            log.warn(
                    "file validation failed:unsupported content type:{}",
                    contentType
            );

            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Unsupported file type"
            );
        }
    }
    private void validateImageContent(BufferedImage image) {
            if (image==null){
                log.warn(
                        "file validation failed:file is not a valid image"
                );

                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Corrupted or invalid image"
                );
            }
    }
    private void validateImageDimension(BufferedImage image){

            if (image==null) return;
            int height=image.getHeight();
            int width=image.getWidth();
            if (height>MAX_IMAGE_HEIGHT || width>MAX_IMAGE_WIDTH){
                log.warn(
                        "file validation failed:image dimension exceeded,width:{},height:{}",
                        width,
                        height
                );
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Image dimension exceeded allowed limit"
                );
            }
    }
    private BufferedImage extractImage(MultipartFile file) {
        try {

        return ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            log.error(
                    "failed to extract image from:{}",
                    file.getOriginalFilename(),
                    e
            );
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Unable to validate image dimension"
            );
        }
    }
}
