package in.shivam.retaillite.inventory.service;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.dto.StockUpdateRequest;
import in.shivam.retaillite.inventory.dto.InventoryResponse;
import in.shivam.retaillite.inventory.dto.ThresholdUpdateRequest;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.exception.QuantityOutOfBoundException;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;

    @Transactional
    @Override
    public InventoryResponse addStock(
            String productId,
            StockUpdateRequest quantity
    ) {
        Inventory inventory=inventoryRepository.findByProduct_productId(productId).orElseThrow(
                ()-> new ResourceNotFoundException("Inventory not found for the product: "+productId));
        Integer availableQuantity=inventory.getAvailableQuantity();
        inventory.setAvailableQuantity(availableQuantity+quantity.getQuantity());
        Inventory updatedInventory =inventoryRepository.save(inventory);
        return toInventoryResponse(updatedInventory);
    }

    @Transactional(readOnly = true)
    @Override
    public InventoryResponse getStock(String productId) {
        Inventory inventory= inventoryRepository.findByProduct_productId(productId).orElseThrow(()->new ResourceNotFoundException("Inventory not found."));
        return toInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<InventoryResponse> getAll(
            int page,
            int size,
            String sortBy,
            String orderedBy
    ) {
        List<String> allowedSortBy= Arrays.asList(
                "inventoryId",
                "productId",
                "availableQuantity",
                "lowStockThreshold",
                "updatedAt"
        );
        if (!allowedSortBy.contains(sortBy)) sortBy="availableQuantity";
        Sort sort="ASC".equalsIgnoreCase(orderedBy)?
                Sort.by(sortBy).ascending():
                Sort.by(sortBy).descending();
        Pageable pageable= PageRequest.of(page,size,sort);
        return inventoryRepository.findAll(pageable)
                .map(this::toInventoryResponse);
    }

    @Transactional
    @Override
    public InventoryResponse removeStock(String productId, StockUpdateRequest quantity) {

        Inventory inventory =inventoryRepository.findByProduct_productId(productId).orElseThrow(()->
                new ResourceNotFoundException("Inventory not found"));

        if (inventory.getAvailableQuantity()-quantity.getQuantity()<0){
            log.warn("Available quantity is {} and remove quantity is {}",inventory.getAvailableQuantity(),quantity.getQuantity());
            throw new QuantityOutOfBoundException("Quantity"+quantity.getQuantity()+" is greater than available quantity");
        }

        Integer availableQuantity=inventory.getAvailableQuantity();
        inventory.setAvailableQuantity(availableQuantity-quantity.getQuantity());
        Inventory inventory1=inventoryRepository.save(inventory);
        log.debug("{} Stock is removed for product:{} Available quantity{}",quantity.getQuantity(),productId,availableQuantity);
        return toInventoryResponse(inventory1);
    }



    @Transactional(readOnly = true)
    @Override
    public List<InventoryResponse> lowStock() {
        return inventoryRepository.findLowStock()
                .stream()
                .map(this::toInventoryResponse)
                .toList();
    }


    @Transactional
    @Override
    public InventoryResponse setThreshold(String productId, ThresholdUpdateRequest threshold) {
        Inventory inventory=inventoryRepository.findByProduct_productId(productId)
                .orElseThrow(()->new ResourceNotFoundException("inventory not found"));
        inventory.setLowStockThreshold(threshold.getThreshold());
        Inventory updatedInventory = inventoryRepository.save(inventory);
        return toInventoryResponse(updatedInventory);
    }

    @Override
    public void validate(Product product, Integer quantity) {
        Inventory inventory=inventoryRepository.findByProduct(product)
                .orElseThrow(()->new ResourceNotFoundException("Product not found."));
        if (inventory.getAvailableQuantity()<quantity){
            throw new QuantityOutOfBoundException("Stock is running out......plz try check again some time.....");
        }
    }

    @Override
    @Transactional
    public void deductStock(Product product, Integer quantity) {
            Inventory inventory=inventoryRepository.findByProduct(product)
                    .orElseThrow(()->new ResourceNotFoundException("Product not found.."));
            Integer inventoryAvailableQuantity=inventory.getAvailableQuantity();
            inventory.setAvailableQuantity(inventoryAvailableQuantity-quantity);
            inventoryRepository.save(inventory);
    }

    private InventoryResponse toInventoryResponse(Inventory inventory){
        return InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .productId(inventory.getProduct().getProductId())
                .productName(inventory.getProduct().getName())
                .availableQuantity(inventory.getAvailableQuantity())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
