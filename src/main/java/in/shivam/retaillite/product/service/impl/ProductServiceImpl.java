package in.shivam.retaillite.product.service.impl;

import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.category.repository.CategoryRepository;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.product.dto.ProductRequest;
import in.shivam.retaillite.product.dto.ProductResponse;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.product.repository.ProductRepository;
import in.shivam.retaillite.product.service.ProductService;
import in.shivam.retaillite.product.validation.ProductImageValidation;
import in.shivam.retaillite.storage.service.StorageService;
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


@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final InventoryRepository inventoryRepository;

    private final ProductRepository productRepository;
    private final ProductImageValidation productImageValidation;
    private  final StorageService storageService;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(
            @Qualifier("localStorageService")
            StorageService storageService,
            ProductRepository productRepository,
            ProductImageValidation productImageValidation,
            CategoryRepository categoryRepository,
            InventoryRepository inventoryRepository) {
        this.storageService = storageService;
        this.productRepository = productRepository;
        this.productImageValidation = productImageValidation;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public ProductResponse create(
            ProductRequest request,
            MultipartFile productImg
    ) {
        productImageValidation.validate(productImg);
        String imagekey= null;
        try {
            imagekey=storageService.upload(productImg,"product");
            Product product=toProduct(request,imagekey);
            Product savedProduct=productRepository.save(product);
            inventoryRepository.save(
                    Inventory.builder()
                            .inventoryId(UUID.randomUUID().toString())
                            .product(product)
                            .availableQuantity(0)
                            .lowStockThreshold(5)
                            .active(true)
                            .build()
            );
            log.info("product is saved and inventory is initialized with quantity 0");
            return toProductResponse(savedProduct);
        } catch (Exception e) {
            if (imagekey!=null){
                log.debug("deleting file:{}\nproblem to upload product in database",imagekey);
                storageService.delete(imagekey);
            }
            throw e;
        }
    }


    @Override
    public void delete(String productId) throws ResourceNotFoundException {
        log.debug("deleting product with productId:{}",productId);
        Product product= productRepository.findByProductId(productId)
                .orElseThrow(
                        ()->(new ResourceNotFoundException("product not found:"+productId))
                );
        productRepository.delete(product);
        try {
            storageService.delete(product.getImageKey());
            log.debug("Deleted product image from the storage successfully");
        }catch (Exception e)
        {
            log.warn("Failed to delete product image: {} form the storage ", product.getImageKey());
            throw new RuntimeException("Failed to delete product image from the storage:"+product.getImageKey());
        }
    }

    @Override
    public Page<ProductResponse> fetchAll(
            int page,
            int size,
            String sortBy,
            String orderedBy
    ) {
        List<String> allowedSortBy= Arrays.asList(
                "name",
                "productId",
                "id"
        );
        if (!allowedSortBy.contains(sortBy)) sortBy="name";
        Sort sort= orderedBy.equalsIgnoreCase("asc")?
                Sort.by(sortBy).ascending():
                Sort.by(sortBy).descending();
        Pageable requestPage= PageRequest.of(page,size,sort );
        return productRepository.findAll(requestPage)
                .map(this::toProductResponse);
    }
    private ProductResponse toProductResponse(Product savedProduct) {
        return ProductResponse.builder()
                .productId(savedProduct.getProductId())
                .name(savedProduct.getName())
                .price(savedProduct.getPrice())
                .taxRate(savedProduct.getTaxRate())
                .description(savedProduct.getDescription())
                .imgUrl(
                        storageService.getFileUrl(savedProduct.getImageKey())
                ).createdAt(savedProduct.getCreatedAt())
                .updatedAt(savedProduct.getUpdatedAt())
                .categoryId(savedProduct.getCategory().getCategoryId())
                .build();
    }

    private Product toProduct(ProductRequest request, String imageKey) {
        log.debug("fetching category for categoryId:{}",request.getCategoryId());
        Category category = categoryRepository
                .findByCategoryId(
                        request.getCategoryId()
                ).orElseThrow(
                        () -> (new ResourceNotFoundException("category does not exist with id: " + request.getCategoryId()))
                );
        log.debug("category found");
        return Product.builder()
                .productId(UUID.randomUUID().toString())
                .name(request.getName())
                .price(request.getPrice())
                .taxRate(request.getTaxRate())
                .description(request.getDescription())
                .imageKey(imageKey)
                .category(category)
                .build();
    }
}
