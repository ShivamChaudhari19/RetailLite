package in.shivam.retaillite.category.service.impl;

import in.shivam.retaillite.category.dto.CategoryRequest;
import in.shivam.retaillite.category.dto.CategoryResponse;
import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.category.exception.CategoryAlreadyExists;
import in.shivam.retaillite.category.repository.CategoryRepository;
import in.shivam.retaillite.category.service.CategoryService;
import in.shivam.retaillite.category.validation.CategoryImageValidation;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryImageValidation categoryImageValidation;
    private final StorageService storageService;
    public CategoryServiceImpl(
            @Qualifier(value = "localStorageService")
            StorageService storageService,
            CategoryRepository categoryRepository,
            CategoryImageValidation categoryImageValidation
    ){
        this.categoryRepository = categoryRepository;
        this.categoryImageValidation = categoryImageValidation;
        this.storageService=storageService;
    }
    @Override
    public Page<CategoryResponse> fetch(
            int page,
            int size,
            String sortBy,
            String orderedBy
    ) {
        List<String> allowedSorting= Arrays.asList(
                "id",
                "userid",
                "name"
        );
        if (!allowedSorting.contains(sortBy.toLowerCase())) sortBy="id";
        Sort sort=orderedBy.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
        Pageable pageable= PageRequest.of(page,size,sort);
        return categoryRepository.findAll(pageable)
                .map(this::toCategoryResponse);
    }

    @Override
    public void delete(String categoryId) throws ResourceNotFoundException {
        Category category= categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("category not found: "+categoryId));
        categoryRepository.delete(category);
        try {
            storageService.delete(category.getImageKey());
        }catch (Exception e)
        {
            log.error("failed to delete category image:{}",
                    category.getImageKey(),
                    e);
        }
    }

    @Override
    public CategoryResponse create(CategoryRequest category, MultipartFile file) {
        if(
                categoryRepository.existsByNameIgnoreCase(category.getName())
        ){
            log.warn(
                    "Failed Category creation: Category {} already exists",
                    category.getName()
            );
            throw new CategoryAlreadyExists("Failed Category creation: Category "+category.getName()+" already exists");
        }
        categoryImageValidation.validate(file);
        String key=null;
        try {
            key=storageService.upload(file,"category");
            return toCategoryResponse(
                    categoryRepository.save(
                            toCategory(category,key)
                    )
            );
        } catch (Exception e) {
            if (key!=null){
                log.warn("category failed to store in DB rollback uploaded image");
                storageService.delete(key);
            }
            throw new RuntimeException(e);
        }
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .imgUrl(
                        storageService.getFileUrl(category.getImageKey())
                ).createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
    private  Category toCategory(CategoryRequest categoryRequest, String imageKey){
        return Category.builder()
                .categoryId(UUID.randomUUID().toString())
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .imageKey(imageKey)
                .build();
    }
}
