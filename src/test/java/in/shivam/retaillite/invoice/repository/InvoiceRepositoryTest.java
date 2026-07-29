package in.shivam.retaillite.invoice.repository;

import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    class FindByInvoiceId {


        @Test
        void shouldReturnInvoiceWhenInvoiceIdExists() {

            buildInvoice("INV-001");

            Optional<Invoice> result =
                    invoiceRepository.findByInvoiceId("INV-001");

            assertThat(result).isPresent();
            assertThat(result.get().getInvoiceId())
                    .isEqualTo("INV-001");
        }

        @Test
        void shouldReturnEmptyWhenInvoiceIdDoesNotExist() {

            Optional<Invoice> result =
                    invoiceRepository.findByInvoiceId("INVALID");

            assertThat(result).isEmpty();
        }

        @Test
        void shouldFetchUserInvoiceItemsAndProducts() {

            buildInvoice("INV-001");


            Invoice result =
                    invoiceRepository.findByInvoiceId("INV-001").orElseThrow(()->new ResourceNotFoundException(""));

            PersistenceUnitUtil util =
                    entityManager.getEntityManager()
                            .getEntityManagerFactory()
                            .getPersistenceUnitUtil();

            assertThat(util.isLoaded(result.getUser()))
                    .isTrue();

            assertThat(util.isLoaded(result.getInvoiceItems()))
                    .isTrue();

            result.getInvoiceItems()
                    .forEach(item ->
                            assertThat(util.isLoaded(item.getProduct()))
                                    .isTrue());
        }
    }

    @Nested
    class FindAllInvoiceByInvoiceStatus{

    }





    private Category persistCategory(){
        Category category= Category.builder()
                .categoryId("CAT-1001")
                .name("Electronics")
                .description("Electronic gadgets and items")
                .imageKey("categories/electronics.png")
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .build();
        return entityManager.persist(category);
    }

    private void persistInventory(Product product){
        Inventory inventory = Inventory.builder()
                .inventoryId("INV-"+product.getProductId())
                .product(product)
                .availableQuantity(100)
                .lowStockThreshold(10)
                .active(true)
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .version(0L) // Set initial version for optimistic locking if applicable
                .build();
        entityManager.persist(inventory);
    }

    private Product persistProduct(Category category){
        Product product = Product.builder()
                .productId("PROD-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(1000))
                .taxRate(BigDecimal.valueOf(18))
                .description("Test product description for invoice test")
                .imageKey("default-image-key.jpg")
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .category(category)
                .inventory(null)
                .build();
        return entityManager.persist(product);
    }

    private User persistUser(){
        User user= User.builder()
                .userId("USR-5005")
                .name("John Doe")
                .username("john doe")
                .password("securePassword123")
                .role(Role.ROLE_ADMIN)
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .isEnable(true)
                .build();
        return entityManager.persist(user);
    }

    private Invoice persistInvoice(String invoiceId,User user){
        Invoice invoice=Invoice.builder()
                .invoiceId(invoiceId)
                .user(user)
                .customerName("customer")
                .customerNumber("0123456789")
                .customerEmail("customer@retaillite.com")
                .subTotal(BigDecimal.valueOf(1000L))
                .tax(BigDecimal.valueOf(180))
                .grandTotal(BigDecimal.valueOf(1800))
                .invoiceStatus(InvoiceStatus.PAID)
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .build();
        return entityManager.persist(invoice);
    }

    private void persistPayment(Invoice invoice){
        Payment payment=Payment.builder()
                .paymentId("PAYID-001")
                .invoice(invoice)
                .paymentMethod(PaymentMethod.CASH)
                .paymentStatus(PaymentStatus.SUCCESS)
                .createdAt(Timestamp.from(Instant.now()))
                .build();
        entityManager.persist(payment);
    }

    private void persistInvoiceItem(Invoice invoice, Product product){
        InvoiceItem item=InvoiceItem.builder()
                .invoiceItemId("INV-001")
                .product(product)
                .invoice(invoice)
                .quantity(1)
                .unitPrice(product.getPrice())
                .lineTotal(product.getPrice())
                .build();
        entityManager.persist(item);
    }


    private void buildInvoice(String invoiceId){
        Category category=persistCategory();
        Product product=persistProduct(category);
        persistInventory(product);
        User user=persistUser();
        Invoice invoice=persistInvoice(invoiceId,user);
        persistInvoiceItem(invoice,product);
        persistPayment(invoice);
        entityManager.flush();
        entityManager.clear();
    }

}