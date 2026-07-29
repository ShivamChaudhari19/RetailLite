package in.shivam.retaillite.product.service;

import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.category.repository.CategoryRepository;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.product.dto.ProductRequest;
import in.shivam.retaillite.product.dto.ProductResponse;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.product.repository.ProductRepository;
import in.shivam.retaillite.product.service.impl.ProductServiceImpl;
import in.shivam.retaillite.product.validation.ProductImageValidation;
import in.shivam.retaillite.storage.service.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {
    
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StorageService localStorageService;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ProductImageValidation productImageValidation;
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    
    
    @Test
    void shouldCreateProduct(){

        ProductRequest request=new ProductRequest("productName", BigDecimal.ONE,BigDecimal.ZERO,"product Description","valid category Id");
        MultipartFile file=getMockMultipartFile();
        Category category=mock(Category.class);
        Inventory inventory=mock(Inventory.class);
        Product savedProduct=Product.builder()
                .id(null)
                .productId("productId")
                .name("name")
                .price(null)
                .taxRate(request.getTaxRate())
                .description(request.getDescription())
                .imageKey("imgKey").createdAt(null)
                .updatedAt(null)
                .category(category)
                .inventory(inventory)
                .build();
        doNothing().when(productImageValidation).validate(file);
        when(localStorageService.upload(any(MultipartFile.class),anyString())).thenReturn("imageKey");
        when(categoryRepository.findByCategoryId(anyString())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(inventoryRepository.save(any(Inventory.class ))).thenReturn(inventory);
        when(localStorageService.getFileUrl(anyString())).thenReturn("url://img");
        ProductResponse response=productService.create(request,file);
        assertEquals(response.getName(),savedProduct.getName());
        assertNotNull(response);
    }

    @Test
    void shouldThrowResourceNotFoundException_WhenCategoryIsMissing(){
        ProductRequest request=new ProductRequest(
                "product1",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                "description",
                "catId"
        );
        MultipartFile file=getMockMultipartFile();
        when(categoryRepository.findByCategoryId(anyString())).thenThrow(ResourceNotFoundException.class);
        assertThrows(ResourceNotFoundException.class,()->productService.create(request,file));
        verifyNoInteractions(productRepository);
        verifyNoInteractions(productImageValidation);
        verifyNoInteractions(localStorageService);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void shouldDeleteSavedFile_WhenThrowsRuntimeException(){
        ProductRequest request=new ProductRequest("productName", BigDecimal.ONE,BigDecimal.ZERO,"product Description","valid category Id");
        MultipartFile file=getMockMultipartFile();
        Category category=mock(Category.class);
        when(categoryRepository.findByCategoryId(anyString())).thenReturn(Optional.of(category));
        doNothing().when(productImageValidation).validate(file);
        when(localStorageService.upload(any(MultipartFile.class),anyString())).thenReturn("imageKey");
        when(productRepository.save(any(Product.class))).thenThrow(RuntimeException.class);
        doNothing().when(localStorageService).delete(anyString());
        assertThrows(RuntimeException.class,()->productService.create(request,file) );
        verify(localStorageService,times(1)).delete(anyString());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void shouldDeleteProduct(){
        String productId="productId";
        Product product=Product.builder()
                .id(null)
                .productId("productId")
                .name("name")
                .price(null)
                .taxRate(null)
                .description(null)
                .imageKey("imgKey").createdAt(null)
                .updatedAt(null)
                .category(null)
                .inventory(null)
                .build();
        when(productRepository.findByProductId(anyString())).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));
        doNothing().when(localStorageService).delete(anyString());
        productService.delete(productId);
        verify(productRepository,times(1)).delete(any(Product.class));
        verify(localStorageService,times(1)).delete(anyString());
    }

    @Test
    void shouldThrowResourceNotFound_WhenProductIsMissing(){
        String productId="productId";
        when(productRepository.findByProductId(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()->productService.delete(productId));
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(localStorageService);
    }
    @Test
    void shouldThrowRuntimeException_WhenFailedToDeleteImage(){
        String productId="productId";
        Product product=Product.builder()
                .id(null)
                .productId("productId")
                .name("name")
                .price(null)
                .taxRate(null)
                .description(null)
                .imageKey("imgKey").createdAt(null)
                .updatedAt(null)
                .category(null)
                .inventory(null)
                .build();
        when(productRepository.findByProductId(anyString())).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));
        doThrow(RuntimeException.class).when(localStorageService).delete(anyString());
        assertThrows(RuntimeException.class,()->productService.delete(productId));
        verifyNoMoreInteractions(localStorageService);
    }

    @Test
    void shouldReturnProductPage(){
        int page=0;
        int size=10;
        String sortBy="xyz";
        String orderedBy="asc";
        Category category=mock(Category.class);
        Inventory inventory=mock(Inventory.class);
        List<Product> products=List.of(
                Product.builder().id(null).productId("productId")
                        .name("name")
                        .price(null)
                        .taxRate(BigDecimal.ZERO)
                        .description("")
                        .imageKey("imgKey").createdAt(null)
                        .updatedAt(null)
                        .category(category)
                        .inventory(inventory)
                        .build(),
                Product.builder().id(null).productId("productId")
                        .name("name1")
                        .price(null)
                        .taxRate(BigDecimal.ZERO)
                        .description("")
                        .imageKey("imgKey1").createdAt(null)
                        .updatedAt(null)
                        .category(category)
                        .inventory(inventory)
                        .build()
        );
        Page<Product> productPage=new PageImpl<>(
                products
        );
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(productPage);
        Page<ProductResponse> responsePage=productService.fetchAll(page,size,sortBy,orderedBy);
        assertEquals(responsePage.getTotalElements(),productPage.getTotalElements());
        assertEquals(responsePage.getTotalPages(),productPage.getTotalPages());
    }
    @Test
    void shouldReturnEmptyPage_WhenNoProductsInDB(){
        int page=0;
        int size=10;
        String sortBy="productId";
        String orderedBy="ASC";
        Category category=mock(Category.class);
        Inventory inventory=mock(Inventory.class);
        List<Product> products= Collections.emptyList();
        Page<Product> productPage=new PageImpl<>(products);
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(productPage);
        Page<ProductResponse> responsePage=productService.fetchAll(page,size,sortBy,orderedBy);
        assertEquals(responsePage.getTotalElements(),productPage.getTotalElements());
        assertEquals(responsePage.getTotalPages(),productPage.getTotalPages());
    }
    private MockMultipartFile getMockMultipartFile(){
        return new MockMultipartFile(
                "mock",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                "multipart file byte stream".getBytes()
        );
    }
}
