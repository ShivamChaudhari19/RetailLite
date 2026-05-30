package in.shivam.retaillite.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.product.dto.ProductRequest;
import in.shivam.retaillite.product.dto.ProductResponse;
import in.shivam.retaillite.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;


    @PreAuthorize("hasRole('ADMIN',)")
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @RequestPart("product")
            String product,
            @RequestPart("productImg")
            MultipartFile productImg
    ){
        ProductRequest request=null;
        try {
            ObjectMapper objectMapper=new ObjectMapper();
             request=objectMapper.readValue(product,ProductRequest.class);
             log.debug("converted product Content-Type text to JSON Content-Type");
        } catch (JsonProcessingException e) {
            log.debug("Failed to convert product Content-Type text to JSON Content-Type: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Product");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        productService.create(request,productImg)
                );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}")
    public void delete(
            @PathVariable
            String productId
    ){
        log.debug("deleting product with productId:{}", productId);
        productService.delete(productId);
        return;
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> fetchAll(
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "10")
            int size,
            @RequestParam(defaultValue = "name")
            String sortBy,
            @RequestParam(defaultValue = "asc")
            String orderedBy
    ){
        log.debug("fetching all product in page with page:{}, size:{}, sortBy:{}, orderedBy:{}",page,size,sortBy,orderedBy);
        return ResponseEntity.ok(
                productService.fetchAll(page,size,sortBy,orderedBy)
        );
    }
}
