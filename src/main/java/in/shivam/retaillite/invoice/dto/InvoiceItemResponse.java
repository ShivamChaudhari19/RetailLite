package in.shivam.retaillite.invoice.dto;

import in.shivam.retaillite.product.entity.Product;
import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InvoiceItemResponse(
        String invoiceItemId,
        String  productId,
        BigDecimal taxRate,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
