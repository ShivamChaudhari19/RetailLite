package in.shivam.retaillite.product.repository;

import in.shivam.retaillite.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findByProductId(String productId);
}
