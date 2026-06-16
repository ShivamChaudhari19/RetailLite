package in.shivam.retaillite.invoice.entity;

import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.payment.entity.Payment;
import in.shivam.retaillite.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true
    )
    private String invoiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    private String customerName;
    private String customerNumber;
    private String customerEmail;


    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal grandTotal;


    @Enumerated(EnumType.STRING)
    private InvoiceStatus invoiceStatus;

    @OneToMany(mappedBy = "invoice", cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private List<Payment> payment;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "invoiceId")
    private List<InvoiceItem> invoiceItems;
    @Version
    private Long version;
}
