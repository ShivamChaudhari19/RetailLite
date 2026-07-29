package in.shivam.retaillite.invoice.mapper;

import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class InvoiceMapper {
    private final InvoiceItemMapper invoiceItemMapper;
    public Invoice toInvoice(InvoiceRequest invoiceRequest,
                             User user,
                             BigDecimal subTotal,
                             BigDecimal tax,
                             BigDecimal grandTotal,
                             InvoiceStatus invoiceStatus,
                             List<InvoiceItem> invoiceItems
    ){
        String date= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int randomSeq= ThreadLocalRandom.current().nextInt(10000);
        String seqPart=String.format("%05d",randomSeq);
        String invoiceId="INV-"+date+"-"+seqPart;
        return Invoice.builder()
                .invoiceId(invoiceId)
                .user(user)
                .customerName(invoiceRequest.customerName())
                .customerNumber(invoiceRequest.customerNumber())
                .customerEmail(invoiceRequest.customerEmail())
                .subTotal(subTotal)
                .tax(tax)
                .grandTotal(grandTotal)
                .invoiceStatus(invoiceStatus)
                .invoiceItems(invoiceItems)
                .build();
    }
    public InvoiceResponse toInvoiceResponse(Invoice invoice){
        return  InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .userName(invoice.getUser().getUsername())
                .customerName(invoice.getCustomerName())
                .customerNumber(invoice.getCustomerNumber())
                .customerEmail(invoice.getCustomerEmail())
                .subTotal(invoice.getSubTotal())
                .tax(invoice.getTax())
                .grandTotal(invoice.getGrandTotal())

                .invoiceStatus(invoice.getInvoiceStatus())

                .invoiceItems(
                        invoice.getInvoiceItems().stream()
                                .map(invoiceItemMapper::toInvoiceItemResponse)
                                .toList()
                ).createdAt(invoice.getCreatedAt())
                .build();
    }
}
