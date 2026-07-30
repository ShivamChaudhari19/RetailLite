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
        BigDecimal unitPrice=product.getPrice();
        BigDecimal taxRate=product.getTaxRate();
        BigDecimal lineTotal= unitPrice.add(unitPrice.multiply(taxRate.divide(BigDecimal.valueOf(100d)))).multiply(BigDecimal.valueOf(invoiceItemRequest.quantity()));
        return InvoiceItem.builder()
                .invoiceItemId(UUID.randomUUID().toString())
                .product(product)
                .quantity(invoiceItemRequest.quantity())
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
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
