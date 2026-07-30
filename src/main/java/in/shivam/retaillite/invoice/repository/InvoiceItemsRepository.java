package in.shivam.retaillite.invoice.repository;

import in.shivam.retaillite.invoice.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InvoiceItemsRepository extends JpaRepository<InvoiceItem,Long> {

    @Query(value = """
                    SELECT COUNT(i)>0 FROM InvoiceItem i
                    JOIN i.product p
                    WHERE p.productId=:productId
                    """)
    boolean existsByProduct_productId(String  productId);

    Optional<InvoiceItem> findByInvoiceItemId(String invoiceItemId);
}
