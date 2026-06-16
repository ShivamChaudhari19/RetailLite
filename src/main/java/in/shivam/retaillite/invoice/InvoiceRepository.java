package in.shivam.retaillite.invoice;

import in.shivam.retaillite.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    Optional<Invoice> findByInvoiceId(String invoiceId);
}
