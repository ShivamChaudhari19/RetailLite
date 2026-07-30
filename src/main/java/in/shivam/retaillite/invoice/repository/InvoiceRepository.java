package in.shivam.retaillite.invoice.repository;

import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    @Query(
            value = """
                        SELECT DISTINCT i from Invoice i
                            JOIN FETCH i.user
                            JOIN FETCH i.invoiceItems items
                            JOIN FETCH items.product
                        WHERE i.invoiceId=:invoiceId"""
    )

    Optional<Invoice> findByInvoiceId(@Param("invoiceId") String invoiceId);

//    @Query(
//            value = """
//                        SELECT i FROM Invoice i
//                            JOIN FETCH i.user
//                            JOIN FETCH i.invoiceItems items
//                            JOIN FETCH items.product
//                        WHERE invoiceStatus=:invoiceStatus""",
//            countQuery = "SELECT count(i) FROM Invoice i WHERE invoiceStatus=:invoiceStatus"
//    )
//    Page<Invoice> findByInvoiceStatus(Pageable pageable,InvoiceStatus invoiceStatus);
    @Query(
            value = "SELECT DISTINCT i from Invoice i where i.invoiceStatus=:invoiceStatus",
            countQuery = "SELECT count(i) from Invoice i where i.invoiceStatus=:invoiceStatus"
    )
    Page<Invoice> findAllInvoiceByInvoiceStatus(Pageable pageable,InvoiceStatus invoiceStatus);
    @Query(
            value = """
                        SELECT DISTINCT i FROM Invoice i
                        JOIN FETCH i.user
                        JOIN FETCH i.invoiceItems items
                        JOIN FETCH items.product
                        WHERE i.id IN :ids"""
    )
    List<Invoice> findByInvoiceIds(Set<Long> ids);


    @Query(
            value = "SELECT DISTINCT i from Invoice i",
            countQuery = "select count(i) from Invoice i"
    )
    Page<Invoice> findAllInvoices(Pageable pageable);
    @Query(
            value= """
                       SELECT DISTINCT i FROM Invoice i
                       JOIN FETCH i.user
                       JOIN FETCH i.invoiceItems items
                       JOIN FETCH items.product
                       WHERE i.id in :ids""",
            countQuery="SELECT count(i) FROM Invoice i"
    )
    List<Invoice> findAllInvoiceAndUsers(List<Long> ids);

}
