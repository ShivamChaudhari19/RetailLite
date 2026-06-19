package in.shivam.retaillite.invoice.repository;

import in.shivam.retaillite.invoice.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemsRepository extends JpaRepository<InvoiceItem,Long> {
}
