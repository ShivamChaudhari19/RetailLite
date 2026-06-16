package in.shivam.retaillite.invoice.dto;

import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Builder
public record InvoiceResponse(

        String invoiceId,

        String userName,

        String customerName,
        String customerNumber,
        String customerEmail,


        BigDecimal subTotal,
        BigDecimal tax,
        BigDecimal grandTotal,


        InvoiceStatus invoiceStatus,


        List<InvoiceItemResponse> invoiceItems,
        Timestamp createdAt,
        Timestamp updatedAt
) {}
