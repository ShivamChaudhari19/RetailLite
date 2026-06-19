package in.shivam.retaillite.invoice.repository;

import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    Optional<Invoice> findByInvoiceId(String invoiceId);
    List<Invoice> findAll();
    Page<Invoice> findByInvoiceStatus(Pageable pageable,InvoiceStatus invoiceStatus);
}
