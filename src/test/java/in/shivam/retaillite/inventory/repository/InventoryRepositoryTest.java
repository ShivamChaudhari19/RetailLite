package in.shivam.retaillite.inventory.repository;


import in.shivam.retaillite.inventory.entity.Inventory;

import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.product.repository.ProductRepository;
import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should find inventory by productId")
    void shouldFindInventoryByProductId() {

        Category category=saveCategory();
        Product product = saveProduct("PROD-001",category);
        Inventory inventory = saveInventory(product, 20, 10);

        Optional<Inventory> result =
                inventoryRepository.findByProduct_productId("PROD-001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(inventory.getId());
        assertThat(result.get().getProduct().getProductId())
                .isEqualTo("PROD-001");
    }

    @Test
    @DisplayName("Should return empty when productId does not exist")
    void shouldReturnEmptyWhenProductIdNotFound() {

        Optional<Inventory> result =
                inventoryRepository.findByProduct_productId("INVALID");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return inventories whose quantity is below threshold")
    void shouldFindLowStockInventories() {
        Category category=saveCategory();
        Product p1 = saveProduct("P1",category);
        Product p2 = saveProduct("P2",category);

        saveInventory(p1, 5, 10);
        saveInventory(p2, 30, 10);

        List<Inventory> inventories =
                inventoryRepository.findLowStock();

        assertThat(inventories)
                .hasSize(1);

        assertThat(inventories.getFirst().getProduct().getProductId())
                .isEqualTo("P1");
    }

    @Test
    @DisplayName("Should return empty list when no inventory is low")
    void shouldReturnEmptyListWhenNoLowStockExists() {
        Category category=saveCategory();
        Product product = saveProduct("P1",category);

        saveInventory(product, 50, 10);

        List<Inventory> inventories =
                inventoryRepository.findLowStock();

        assertThat(inventories).isEmpty();
    }

    @Test
    @DisplayName("Should find inventory by product")
    void shouldFindInventoryByProduct() {
        Category category=saveCategory();
        Product product = saveProduct("PROD-001",category);

        Inventory inventory =
                saveInventory(product, 20, 10);

        Optional<Inventory> result =
                inventoryRepository.findByProduct(product);

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(inventory.getId());
    }

    @Test
    @DisplayName("Should return empty when inventory for product does not exist")
    void shouldReturnEmptyWhenInventoryDoesNotExist() {
        Category category=saveCategory();
        Product product = saveProduct("PROD-001",category);

        Optional<Inventory> result =
                inventoryRepository.findByProduct(product);

        assertThat(result).isEmpty();
    }

    // ---------------- Helper Methods ----------------

    private Category saveCategory(){
        Category category = Category.builder()
                .categoryId("CAT-001")
                .name("Electronics")
                .description("Category")
                .build();
        return categoryRepository.save(category);
    }

    private Product saveProduct(String productId, Category category) {
        Product product = Product.builder()
                .productId(productId)
                .name("Laptop")
                .description("Laptop")
                .price(BigDecimal.valueOf(1000))
                .taxRate(BigDecimal.valueOf(18))
                .category(category)
                .build();
        return productRepository.save(product);
    }

    private Inventory saveInventory(
            Product product,
            int availableQuantity,
            int lowStockThreshold
    ) {

        Inventory inventory = Inventory.builder()
                .inventoryId("INV-" + product.getProductId())
                .product(product)
                .availableQuantity(availableQuantity)
                .lowStockThreshold(lowStockThreshold)
                .active(true)
                .build();

        return inventoryRepository.save(inventory);
    }
}