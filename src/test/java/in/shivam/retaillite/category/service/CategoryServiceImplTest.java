package in.shivam.retaillite.category.service;

import in.shivam.retaillite.category.dto.CategoryRequest;
import in.shivam.retaillite.category.dto.CategoryResponse;
import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.category.exception.CategoryAlreadyExists;
import in.shivam.retaillite.category.exception.CategoryDeletionException;
import in.shivam.retaillite.category.repository.CategoryRepository;
import in.shivam.retaillite.category.service.impl.CategoryServiceImpl;
import in.shivam.retaillite.category.validation.CategoryImageValidation;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.product.repository.ProductRepository;
import in.shivam.retaillite.storage.service.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryImageValidation categoryImageValidation;
    @Mock
    private StorageService localStorageService;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryServiceImpl;


    @Test
    void shouldCreateCategory_ForValidCategoryRequestAndMultipartFile(){

        CategoryRequest category= new CategoryRequest("valid category name","valid description for category");
        MultipartFile mockMultipartFile= new MockMultipartFile(
                "mock name",
                "avatar.png",
                "image/png",
                "mock image content".getBytes()
        );

        Category category1=Category.builder()
                        .id(null).categoryId("valid categoryId").name("valid category name").description("valid description for category").imageKey("valid img kay").createdAt(null).updatedAt(null).build();
        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        doNothing().when(categoryImageValidation).validate(mockMultipartFile);
        when(localStorageService.upload(any(MultipartFile.class),anyString())).thenReturn("mock key");
        when(categoryRepository.save(any(Category.class))).thenReturn(category1);
        when(localStorageService.getFileUrl(anyString())).thenReturn("url://mock key url");
        CategoryResponse categoryResponse=categoryServiceImpl.create(category,mockMultipartFile);
        assertEquals(categoryResponse.getName(),category.getName());
        assertEquals(categoryResponse.getDescription(),category.getDescription());
    }
    @Test
    void  shouldDeleteFile_WhenFileIsStoredAndThenThrownException(){
        CategoryRequest category=new CategoryRequest("valid name","valid description");
        MultipartFile file= mock(MultipartFile.class);

        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        doNothing().when(categoryImageValidation).validate(any(MultipartFile.class));
        when(localStorageService.upload(any(MultipartFile.class),anyString())).thenReturn("mock key");
        when(categoryRepository.save(any(Category.class))).thenThrow(RuntimeException.class);
        doNothing().when(localStorageService).delete(anyString());
        assertThrows(RuntimeException.class,()-> categoryServiceImpl.create(category,file));
    }
    @Test
    void shouldThrowCategoryAlreadyExistException_WhenCategoryAlreadyExists(){
        CategoryRequest request=new CategoryRequest("name of category","description");
        MultipartFile file=mock(MultipartFile.class);

        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        assertThrows(CategoryAlreadyExists.class,()->categoryServiceImpl.create(request,file));
    }

    @Test
    void shouldDeleteCategory_WhenCategoryHasNoAssociatedProducts(){
        Category category= Category.builder()
                .id(null).categoryId("validCategory").name("").description("").imageKey("").createdAt(null).updatedAt(null).build();
        when(categoryRepository.findByCategoryId(anyString())).thenReturn(Optional.ofNullable(category));
        when(productRepository.existsByCategory_categoryId(anyString())).thenReturn(false);
        doNothing().when(categoryRepository).delete(any(Category.class));
        doNothing().when(localStorageService).delete(anyString());
        categoryServiceImpl.delete(anyString());
        verify(categoryRepository,times(1)).delete(any(Category.class));
    }
    @Test
    void shouldNotDeleteCategory_WhenCategoryHasAssociatedProducts(){
        Category category= Category.builder()
                .id(null).categoryId("validCategory").name("").description("").imageKey("").createdAt(null).updatedAt(null).build();
        when(categoryRepository.findByCategoryId(anyString())).thenReturn(Optional.of(category));
        when(productRepository.existsByCategory_categoryId(anyString())).thenReturn(true);
        assertThrows(CategoryDeletionException.class,()-> categoryServiceImpl.delete(anyString()));
    }

    @Test
    void shouldThrowResourceNotFoudException_WhenCategoryNotExists(){
        when(categoryRepository.findByCategoryId(anyString())).thenThrow(new ResourceNotFoundException("category not found:"));
        assertThrows(ResourceNotFoundException.class,()->categoryServiceImpl.delete(anyString()));
    }
    @Test
    void shouldCathException_WhenStorageServiceThrowsException(){
        Category category=Category.builder()
                        .id(null).categoryId("validCategory").name(null).description(null).imageKey("non null key").createdAt(null).updatedAt(null).build();
        when(categoryRepository.findByCategoryId(anyString())).thenReturn(Optional.of(category));
        when(productRepository.existsByCategory_categoryId(anyString())).thenReturn(false);
        doNothing().when(categoryRepository).delete(any(Category.class));
        doThrow(RuntimeException.class).when(localStorageService).delete(anyString());
        assertThrows(RuntimeException.class,()->categoryServiceImpl.delete("categoryId"));

    }
    @Test
    void shouldFetchCategoryPage_ForPageRequest() {
        int page = 0;
        int size = 15;
        String sortBy = "creaTedAt";
        String orderedBy = "DESC";
        List<Category> categories = List.of(
                Category.builder().id(232323L).categoryId("cateoryid").name("name").description("description").imageKey("image key1").createdAt(null).updatedAt(null).build(),
                Category.builder().id(24234L).categoryId("categoryId").name("name1").description("description").imageKey("imgKey2").createdAt(null).updatedAt(null).build()
        );
        Page<Category> requestedPage = new PageImpl<>(categories);
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(requestedPage);
        when(localStorageService.getFileUrl(categories.getFirst().getImageKey())).thenReturn("Url://image key1");
        when(localStorageService.getFileUrl(categories.get(1).getImageKey())).thenReturn("url://imgKey2");
        Page<CategoryResponse> categoryResponsePage = categoryServiceImpl.fetch(page, size, sortBy, orderedBy);
        assertNotNull(categoryResponsePage);
        assertEquals(categoryResponsePage.getTotalElements(), categories.size());
    }

    @Test
    @DisplayName("Should return empty list when db has no category for fetch")
    void shouldReturnEmptyPage_ForEmptyResult(){
        Page<Category> emptyPage=new PageImpl<>(List.of());
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(emptyPage  );
        Page<CategoryResponse> categoryResponses=categoryServiceImpl.fetch(0,10,"UPdatedAt","asc");
        assertEquals(emptyPage.getTotalElements(),categoryResponses.getTotalElements());
    }



}
