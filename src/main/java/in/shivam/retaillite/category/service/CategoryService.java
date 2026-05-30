package in.shivam.retaillite.category.service;

import in.shivam.retaillite.category.dto.CategoryRequest;
import in.shivam.retaillite.category.dto.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CategoryService {


    Page<CategoryResponse>  fetch(int page, int size, String sortBy, String orderedBy);
    void delete(String categoryId);
    CategoryResponse create(@Valid CategoryRequest category, MultipartFile categoryImg);
}
