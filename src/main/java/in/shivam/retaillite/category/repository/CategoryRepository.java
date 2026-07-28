package in.shivam.retaillite.category.repository;

import in.shivam.retaillite.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Category> findByCategoryId(String categoryId);
}
