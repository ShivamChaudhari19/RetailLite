package in.shivam.retaillite.invoice.mapper;

import in.shivam.retaillite.invoice.dto.InvoiceItemRequest;
import in.shivam.retaillite.invoice.dto.InvoiceItemResponse;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.product.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class InvoiceItemMapper {
    public InvoiceItem toInvoiceItem(InvoiceItemRequest invoiceItemRequest, Product product){
        return InvoiceItem.builder()
                .invoiceItemId(UUID.randomUUID().toString())
                .product(product)
                .quantity(invoiceItemRequest.quantity())
                .unitPrice(product.getPrice())
                .lineTotal(product.getPrice().add(product.getPrice().multiply(product.getTaxRate()).divide(BigDecimal.valueOf(100))))
                .build();
    }

    public InvoiceItemResponse toInvoiceItemResponse(InvoiceItem invoiceItem) {
        return InvoiceItemResponse.builder()
                .invoiceItemId(invoiceItem.getInvoiceItemId())
                .productId(invoiceItem.getProduct().getProductId())
                .taxRate(invoiceItem.getProduct().getTaxRate())
                .quantity(invoiceItem.getQuantity())
                .unitPrice(invoiceItem.getUnitPrice())
                .lineTotal(invoiceItem.getLineTotal())
                .build();
    }
}
