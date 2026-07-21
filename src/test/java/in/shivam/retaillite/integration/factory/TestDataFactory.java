package in.shivam.retaillite.integration.factory;

import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Component
public class TestDataFactory {
    @Autowired
    private PasswordEncoder passwordEncoder;
    public User getUser(String username, String password, Role role) {
        return User.builder()
                .userId("userid"+username)
                .name("name")
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .isEnable(true)
                .build();
    }


    public Category getCategory(){
        return Category.builder()
                .categoryId("CAT-1001")
                .name("Electronics")
                .description("Electronic gadgets and items")
                .imageKey("categories/electronics.png")
                .build();
    }

    public Inventory getInventory(Product product){
        return Inventory.builder()
                .inventoryId("INV-"+product.getProductId())
                .product(product)
                .availableQuantity(100)
                .lowStockThreshold(10)
                .active(true)
                .build();
    }

    public Product getProduct(Category category){
        return Product.builder()
                .productId("PROD-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(1000))
                .taxRate(BigDecimal.valueOf(18))
                .description("Test product description for invoice test")
                .imageKey("default-image-key.jpg")
                .category(category)
                .build();
    }

    public User getUser(){
        return User.builder()
                .userId("USR-5005")
                .name("John Doe")
                .username("john doe")
                .password("securePassword123")
                .role(Role.ROLE_ADMIN)
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .isEnable(true)
                .build();

    }

    public Invoice getInvoice(String invoiceId,User user,InvoiceStatus invoiceStatus){
        return Invoice.builder()
                .invoiceId(invoiceId)
                .user(user)
                .customerName("customer")
                .customerNumber("0123456789")
                .customerEmail("customer@retaillite.com")
                .subTotal(BigDecimal.valueOf(1000L))
                .tax(BigDecimal.valueOf(180))
                .grandTotal(BigDecimal.valueOf(1800))
                .invoiceStatus(invoiceStatus)
                .build();
       
    }

//    public Payment getPayment(Invoice invoice,PaymentMethod paymentMethod){
//        return Payment.builder()
//                .paymentId("PAYID-001")
//                .invoice(invoice)
//                .paymentMethod(paymentMethod)
//                .paymentStatus(PaymentStatus.SUCCESS)
//                .createdAt(Timestamp.from(Instant.now()))
//                .build();
//
//    }

    public InvoiceItem getInvoiceItem( Product product){
        return InvoiceItem.builder()
                .invoiceItemId("INV-001")
                .product(product)
                .quantity(1)
                .unitPrice(product.getPrice())
                .lineTotal(product.getPrice().multiply(BigDecimal.valueOf(1)))
                .build();
       
    }


//    public Invoice buildInvoice(String invoiceId,InvoiceStatus invoiceStatus){
//        Category category=getCategory();
//        Product product=getProduct(category);
//        getInventory(product);
//        User user=getUser();
//        Invoice invoice=getInvoice(invoiceId,user,invoiceStatus);
//        InvoiceItem invoiceItem=getInvoiceItem(invoice,product);
//        invoice.setInvoiceItems(List.of(invoiceItem));
//        return invoice;
//    }

}
