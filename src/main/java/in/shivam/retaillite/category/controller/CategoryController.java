package in.shivam.retaillite.category.controller;

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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
@Validated
public class CategoryController {
    private final CategoryService categoryService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestPart("category") CategoryRequest request,
            @RequestPart("categoryImg") MultipartFile categoryImg
    ){
        log.debug("Category creating for: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(request, categoryImg));
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/categories")
    public ResponseEntity<Page<CategoryResponse>> fetch(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,
            @RequestParam(defaultValue = "20")
            @Min(0) @Max(50) int size,
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String categoryId){
        log.debug("deleting Category with categoryId: {}",categoryId);
        categoryService.delete(categoryId);
    }
}
