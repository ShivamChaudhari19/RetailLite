package in.shivam.retaillite.payment;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByInvoice_invoiceIdAndPaymentStatus(String invoiceId, PaymentStatus paymentStatus);

    @Query(
            value = """
                    SELECT p from Payment p
                        JOIN FETCH p.invoice i
                    WHERE p.paymentStatus=PaymentStatus.PENDING
                          AND i.invoiceId=:invoiceId
                    ORDER BY p.createdAt DESC"""
    )
    Optional<Payment> findPendingPaymentByInvoiceId(String invoiceId);

    @Query(
            value = """
                SELECT p from Payment p
                JOIN FETCH p.invoice i
                WHERE p.gatewayOrderId=:gatewayOrderId"""
    )
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    Optional<Payment> findByPaymentId(String paymentId);
}
