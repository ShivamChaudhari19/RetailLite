package in.shivam.retaillite.payment;

import in.shivam.retaillite.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByInvoice_invoiceIdAndPaymentStatus(String invoiceId, PaymentStatus paymentStatus);

    @Query(
            value = """
                    SELECT p from Payment p
                        JOIN FETCH i.invoice
                    WHERE p.paymentStatus=PaymentStatus.PENDING
                          AND i.invoiceId=:invoiceId
                    ORDER BY p.createdAt DESC"""
    )
    Optional<Payment> findPendingPaymentByInvoiceId(String invoiceId);
}
