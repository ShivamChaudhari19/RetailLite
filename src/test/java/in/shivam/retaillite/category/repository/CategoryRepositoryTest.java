package in.shivam.retaillite.category.repository;

import in.shivam.retaillite.category.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;


    @Test
    void should_findBy_CategoryId_WhenCategoryExists_ReturnCategoryEntity() {
        Category category=Category.builder()
                .categoryId("catId")
                .build();
        entityManager.persist(category);
        entityManager.flush();
        Optional<Category> existingCategory=categoryRepository.findByCategoryId("catId");
        assertTrue(existingCategory.isPresent());
        assertEquals("catId",existingCategory.get().getCategoryId());
    }
    @Test
    void should_findBy_CategoryId_WhenCategoryNotExists_ReturnNullObject() {
        Optional<Category> existingCategory=categoryRepository.findByCategoryId("notfoundcatId");
        assertFalse(existingCategory.isPresent());
    }

}