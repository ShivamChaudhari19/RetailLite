package in.shivam.retaillite.inventory.service;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.dto.InventoryResponse;
import in.shivam.retaillite.inventory.dto.StockUpdateRequest;
import in.shivam.retaillite.inventory.dto.ThresholdUpdateRequest;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.exception.QuantityOutOfBoundException;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void shouldAddStockQuantity() {
        String productId="product id";
        StockUpdateRequest quantity= new StockUpdateRequest(10);
        Product product=mock(Product.class);
        Inventory inventory=Inventory.builder()
                .inventoryId("inventory id").product(product).availableQuantity(0).lowStockThreshold(5).active(true).build();
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        InventoryResponse inventoryResponse=inventoryService.addStock(productId,quantity);
        assertEquals(inventoryResponse.getAvailableQuantity(),quantity.getQuantity());
    }
    @Test
    void shouldThrowResourceNotFoundException_WhenInventoryIsNotFound(){
        String productId="product id";
        StockUpdateRequest quantity= new StockUpdateRequest(10);
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.empty());
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->inventoryService.addStock(productId,quantity));
        assertEquals("Inventory not found for the product: "+productId,exception.getMessage());
    }

    @Test
    void shouldReturnInventory() {
        String productId="product id";
        Product product=mock(Product.class);
        Inventory inventory=Inventory.builder()
                .inventoryId("inventory id").product(product).availableQuantity(0).lowStockThreshold(5).active(true).build();
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.of(inventory));
        InventoryResponse inventoryResponse=inventoryService.getStock(productId);
        assertEquals(inventoryResponse.getInventoryId(),inventory.getInventoryId());
    }
    @Test
    void shouldThrowResourceNotFoundException_WhenInventoryIsNotFound_ForStockIsRequested(){
        String productId="product id";

        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.empty());
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->inventoryService.getStock(productId));
        assertEquals("Inventory not found for the product: "+productId,exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource(value ={
            "inventoryId,inventoryId,Asc",
            "productId,productId,Desc",
            "lowStockThreshold,lowStockThreshold,Asc",
            "UPDATEDAT,updatedAt,DESC",
            "aVAILABLEqUANTITY,availableQuantity,ASC"
    }
    )
    void shouldFetchAllStock_WhenPageRequestIsValid(String inputSortBy, String expectedSortBy, String orderedBy) {

        Product product=mock(Product.class);
        when(product.getProductId()).thenReturn("productId");
        List<Inventory> inventories=List.of(Inventory.builder().inventoryId("inventory id").product(product).availableQuantity(0).lowStockThreshold(5).active(true).build());
        Page<Inventory>inventoryPage=new PageImpl<>(inventories);
        when(inventoryRepository.findAll(any(Pageable.class))).thenReturn(inventoryPage);
        Page<InventoryResponse>inventoryResponses=inventoryService.getAll(0,10,inputSortBy,orderedBy);



        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(inventoryRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        Sort.Order order=pageable.getSort().iterator().next();
        assertEquals(expectedSortBy, order.getProperty());

        assertEquals(
                Sort.Direction.valueOf(orderedBy.toUpperCase()),
                order.getDirection()
        );
    }

    @Test
    void shouldRemoveStock_WhenQuantityIsAvailable() {

        String productId="productId";
        StockUpdateRequest quantity=new StockUpdateRequest(30);
        Product product=mock(Product.class);
        Inventory inventory= Inventory.builder()
                        .inventoryId("inventory id").product(product).availableQuantity(100).lowStockThreshold(5).active(true).build();
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        InventoryResponse inventoryResponse=inventoryService.removeStock(productId,quantity);
        assertEquals(70,inventoryResponse.getAvailableQuantity());
    }
    @Test
    void shouldThrowResourceNotFoundException_WhenInventoryDoesNotExist(){
        String productId="productId";
        StockUpdateRequest quantity=new StockUpdateRequest(30);
        Product product=mock(Product.class);
        Inventory inventory= Inventory.builder()
                .inventoryId("inventory id").product(product).availableQuantity(100).lowStockThreshold(5).active(true).build();
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->inventoryService.removeStock(productId,quantity));
        assertEquals("Inventory not found for the product: "+productId,exception.getMessage());

    }
    @Test
    void shouldThrowQuantityOutOfBoundException_WhenQuantityExceedsAvailableStock(){
        String productId="productId";
        StockUpdateRequest quantity=new StockUpdateRequest(130);
        Product product=mock(Product.class);
        Inventory inventory= Inventory.builder()
                .inventoryId("inventory id").product(product).availableQuantity(100).lowStockThreshold(5).active(true).build();
        when(inventoryRepository.findByProduct_productId(anyString())).thenReturn(Optional.of(inventory));
        QuantityOutOfBoundException exception =assertThrows(QuantityOutOfBoundException.class,()->inventoryService.removeStock(productId,quantity));
        assertEquals("Quantity"+quantity.getQuantity()+" is greater than available quantity",exception.getMessage());
    }
    @Test
    void lowStock() {
        Product product=Product.builder().productId("product2").build();
        Product product1=Product.builder().productId("product3").build();
        List<Inventory> inventories = List.of(
                Inventory.builder().inventoryId("inventory id").product(product).availableQuantity(1).lowStockThreshold(5).active(true).build(),
                Inventory.builder().inventoryId("inventory id 1").product(product1).availableQuantity(0).lowStockThreshold(1).active(true).build()
        );

        when(inventoryRepository.findLowStock())
                .thenReturn(inventories);
        List<InventoryResponse> responses = inventoryService.lowStock();
        assertEquals(2, responses.size());

        verify(inventoryRepository).findLowStock();
    }
    @Test
    void shouldReturnEmptyList_WhenNoLowStockExists(){
        when(inventoryRepository.findLowStock())
                .thenReturn(Collections.emptyList());
        List<InventoryResponse> responses = inventoryService.lowStock();

        assertTrue(responses.isEmpty());

        verify(inventoryRepository).findLowStock();
    }
    @Test
    void shouldUpdateLowStockThreshold_WhenInventoryExists() {
        ThresholdUpdateRequest threshold=new ThresholdUpdateRequest(10);
        Product product=mock(Product.class);
        Inventory inventory=Inventory.builder()
                .inventoryId("inventory id").product(product).availableQuantity(0).lowStockThreshold(5).active(true).build();

        when(inventoryRepository.findByProduct_productId(anyString()))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        InventoryResponse response =
                inventoryService.setThreshold("product id", threshold);
        ArgumentCaptor<Inventory> captor =
                ArgumentCaptor.forClass(Inventory.class);

        verify(inventoryRepository).save(captor.capture());

        Inventory savedInventory = captor.getValue();

        assertEquals(10, savedInventory.getLowStockThreshold());
    }

    @Test
    void shouldValidate_WhenStockIsAvailable() {

        Product product = Product.builder().productId("P001").build();

        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(20)
                .build();

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        assertDoesNotThrow(() ->
                inventoryService.validate(product, 10));

        verify(inventoryRepository).findByProduct(product);
        verify(inventoryRepository, never()).save(any());
    }
    @Test
    void shouldThrowResourceNotFoundException_WhenInventoryDoesNotExistDuringValidation() {

        Product product = Product.builder().productId("P001").build();

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.validate(product, 5)
        );

        verify(inventoryRepository).findByProduct(product);
        verify(inventoryRepository, never()).save(any());
    }
    @Test
    void shouldThrowQuantityOutOfBoundException_WhenRequestedQuantityExceedsAvailableStockDuringValidation() {

        Product product = Product.builder().productId("P001").build();

        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(5)
                .build();

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                QuantityOutOfBoundException.class,
                () -> inventoryService.validate(product, 10)
        );

        verify(inventoryRepository).findByProduct(product);
        verify(inventoryRepository, never()).save(any());
    }
    @Test
    void shouldDeductStock_WhenStockIsAvailable() {

        Product product = Product.builder().productId("P001").build();

        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(20)
                .build();

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.deductStock(product, 5);

        ArgumentCaptor<Inventory> captor =
                ArgumentCaptor.forClass(Inventory.class);

        verify(inventoryRepository).save(captor.capture());

        Inventory savedInventory = captor.getValue();

        assertEquals(15, savedInventory.getAvailableQuantity());

        verify(inventoryRepository).findByProduct(product);
    }
    @Test
    void shouldThrowResourceNotFoundException_WhenInventoryDoesNotExistDuringStockDeduction() {

        Product product = Product.builder().productId("P001").build();

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.deductStock(product, 5)
        );

        verify(inventoryRepository).findByProduct(product);
        verify(inventoryRepository, never()).save(any());
    }
    @Test
    void shouldThrowQuantityOutOfBoundException_WhenRequestedQuantityExceedsAvailableStockDuringDeduction() {

        Product product = Product.builder().productId("P001").build();

        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(5)
                .build();

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                QuantityOutOfBoundException.class,
                () -> inventoryService.deductStock(product, 10)
        );

        verify(inventoryRepository).findByProduct(product);
        verify(inventoryRepository, never()).save(any());
    }
    @Test
    void shouldAddStock_WhenInventoryExists() {

        Product product = Product.builder()
                .productId("P001")
                .build();

        Inventory inventory = Inventory.builder()
                .product(product)
                .availableQuantity(20)
                .build();

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.addStock(product, 5);

        ArgumentCaptor<Inventory> captor =
                ArgumentCaptor.forClass(Inventory.class);

        verify(inventoryRepository).save(captor.capture());

        Inventory savedInventory = captor.getValue();

        assertEquals(25, savedInventory.getAvailableQuantity());

        verify(inventoryRepository).findByProduct(product);
    }
    @Test
    void shouldThrowResourceNotFoundException_WhenInventoryDoesNotExistDuringStockAddition() {

        Product product = Product.builder()
                .productId("P001")
                .build();

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.addStock(product, 5)
        );

        verify(inventoryRepository).findByProduct(product);
        verify(inventoryRepository, never()).save(any());
    }
}