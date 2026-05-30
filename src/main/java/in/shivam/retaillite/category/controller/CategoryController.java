package in.shivam.retaillite.category.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.category.dto.CategoryRequest;
import in.shivam.retaillite.category.dto.CategoryResponse;
import in.shivam.retaillite.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestPart("category") String category,
            @RequestPart("categoryImg") MultipartFile categoryImg
    ){
        CategoryRequest categoryRequest=null;
        try {
            ObjectMapper mapper=new ObjectMapper();
             categoryRequest=mapper.readValue(category,CategoryRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (categoryImg == null){
            log.warn("file not found for category :{}\n{}",categoryRequest.getName(), getClass());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found");
        }
        log.debug("Category creating for: {}", categoryRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(categoryRequest, categoryImg));
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/categories")
    public ResponseEntity<Page<CategoryResponse>> fetch(
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            @RequestParam(defaultValue = "sortBy")
            String sortBy,
            @RequestParam(defaultValue = "asc")
            String orderedBy
    ){
        log.debug("fetching all categories...");
        return ResponseEntity.ok()
                .body(categoryService.fetch(page,size,sortBy,orderedBy));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public void delete(@PathVariable String categoryId){
        log.debug("deleting Category with categoryId: {}",categoryId);
        categoryService.delete(categoryId);
    }
}
