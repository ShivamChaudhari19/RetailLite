package in.shivam.retaillite.inventory.repository;

import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByProduct_productId(String productId);


    @Query("""
            SELECT i FROM Inventory i WHERE i.availableQuantity<i.lowStockThreshold
            """)
    List<Inventory> findLowStock();

    Optional<Inventory> findByProduct(Product product);
}
