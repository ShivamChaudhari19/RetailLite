package in.shivam.retaillite.invoice.pdf.section;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;
import in.shivam.retaillite.invoice.pdf.style.InvoiceTheme;
import in.shivam.retaillite.invoice.pdf.style.StatusPresentation;
import in.shivam.retaillite.common.enums.PaymentStatus;
import org.springframework.stereotype.Component;

/**
 * Renders payment method/status/reference when the mapped {@link InvoiceDocument}
 * carries them. If none are present (e.g. the source payment record didn't
 * resolve to a populated set of fields), this section renders nothing rather
 * than printing an empty or misleading block.
 */
@Component
public class PaymentSectionRenderer implements InvoiceSectionRenderer {

    @Override
    public void render(Document document, InvoiceDocument invoice, CompanyProperties company) throws DocumentException {
        if (!hasPaymentDetails(invoice)) {
            return ;
        }

        Paragraph label = new Paragraph("PAYMENT DETAILS", InvoiceTheme.FONT_SECTION_LABEL);
        label.setSpacingAfter(InvoiceTheme.SPACING_SMALL);
        document.add(label);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(InvoiceTheme.SPACING_SECTION);

        table.addCell(detailCell("Payment ID", nullSafe(invoice.paymentId())));
        table.addCell(detailCell("Method", StatusPresentation.formatPaymentMethod(invoice.paymentMethod())));
        table.addCell(statusDetailCell(invoice.paymentStatus()));

        document.add(table);
    }

    private boolean hasPaymentDetails(InvoiceDocument invoice) {
        return invoice.paymentId() != null || invoice.paymentMethod() != null || invoice.paymentStatus() != null;
    }

    private PdfPCell detailCell(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Paragraph(label, InvoiceTheme.FONT_BODY_MUTED));
        cell.addElement(new Paragraph(value, InvoiceTheme.FONT_BODY_BOLD));
        return cell;
    }

    private PdfPCell statusDetailCell(PaymentStatus status) {
        StatusPresentation.Badge badge = StatusPresentation.forPaymentStatus(status);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Paragraph("Status", InvoiceTheme.FONT_BODY_MUTED));
        cell.addElement(new Paragraph(badge.label(), InvoiceTheme.statusBadgeFont(badge.textColor())));
        return cell;
    }

    private String nullSafe(String value) {
        return (value == null || value.isBlank()) ? "\u2014" : value;
    }
}
