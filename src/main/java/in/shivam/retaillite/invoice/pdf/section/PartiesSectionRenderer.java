package in.shivam.retaillite.invoice.pdf.section;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;
import in.shivam.retaillite.invoice.pdf.style.InvoiceTheme;
import org.springframework.stereotype.Component;

/**
 * Two-column section beneath the header: who the invoice is billed to, and
 * who within the company issued it.
 */
@Component
public class PartiesSectionRenderer implements InvoiceSectionRenderer {

    @Override
    public void render(Document document, InvoiceDocument invoice, CompanyProperties company) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1f});
        table.setSpacingAfter(InvoiceTheme.SPACING_SECTION);

        table.addCell(buildBillToCell(invoice));
        table.addCell(buildIssuedByCell(invoice));

        document.add(table);

    }

    private PdfPCell buildBillToCell(InvoiceDocument invoice) {
        PdfPCell cell = borderlessCell();
        cell.addElement(new Paragraph("BILL TO", InvoiceTheme.FONT_SECTION_LABEL));
        cell.addElement(new Paragraph(nullSafe(invoice.customerName()), InvoiceTheme.FONT_BODY_BOLD));
        cell.addElement(new Paragraph(nullSafe(invoice.customerNumber()), InvoiceTheme.FONT_BODY_MUTED));
        cell.addElement(new Paragraph(nullSafe(invoice.customerEmail()), InvoiceTheme.FONT_BODY_MUTED));
        return cell;
    }

    private PdfPCell buildIssuedByCell(InvoiceDocument invoice) {
        PdfPCell cell = borderlessCell();
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.addElement(alignedParagraph("ISSUED BY", InvoiceTheme.FONT_SECTION_LABEL, Element.ALIGN_RIGHT));
        cell.addElement(alignedParagraph(nullSafe(invoice.invoiceIssuer()), InvoiceTheme.FONT_BODY_BOLD, Element.ALIGN_RIGHT));
        return cell;
    }

    private Paragraph alignedParagraph(String text, Font font, int alignment) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(alignment);
        return paragraph;
    }

    private PdfPCell borderlessCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(InvoiceTheme.SPACING_SMALL);
        return cell;
    }

    private String nullSafe(String value) {
        return (value == null || value.isBlank()) ? "\u2014" : value;
    }
}
