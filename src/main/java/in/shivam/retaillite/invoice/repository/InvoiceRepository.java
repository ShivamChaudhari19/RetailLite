package in.shivam.retaillite.invoice.repository;

import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    @Query(
            value = """
                        SELECT i from Invoice i
                            JOIN FETCH i.user
                            JOIN FETCH i.invoiceItems items
                            JOIN FETCH items.product
                        WHERE i.invoiceId=:invoiceId"""
    )
    Optional<Invoice> findByInvoiceId(String invoiceId);

    @Query(
            value = """
                        SELECT i FROM Invoice i
                            JOIN FETCH i.user
                            JOIN FETCH i.invoiceItems items
                            JOIN FETCH items.product
                        WHERE invoiceStatus=:invoiceStatus""",
            countQuery = "SELECT count(i) FROM Invoice i"
    )
    Page<Invoice> findByInvoiceStatus(Pageable pageable,InvoiceStatus invoiceStatus);

    @Query(
            value= """
                       SELECT i FROM Invoice i
                       JOIN FETCH i.user
                       JOIN FETCH i.invoiceItems items
                       JOIN FETCH items.product""",
            countQuery="SELECT count(i) FROM Inventory i"
    )
    Page<Invoice> findAllInvoiceAndUsers(Pageable pageable);
    
}
