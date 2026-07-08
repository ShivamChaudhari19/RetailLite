package in.shivam.retaillite.payment.domain.entity;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.invoice.entity.Invoice;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.action.internal.OrphanRemovalAction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "invoiceId")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private PaymentMethod paymentMethod;

    @Column(unique = true)
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String gatewayRefundId;
    private Timestamp gatewayOrderIdCreatedAt;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;
    @Version
    private long version;

    public void markSuccess(){
        setPaymentStatus(PaymentStatus.SUCCESS);
    }
    public void markRefunded(){
        setPaymentStatus(PaymentStatus.REFUNDED);
    }
    public void markExpired(){
        setPaymentStatus(PaymentStatus.EXPIRED);
    }
}
