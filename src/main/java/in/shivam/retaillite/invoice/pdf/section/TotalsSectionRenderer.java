package in.shivam.retaillite.invoice.pdf.section;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;
import in.shivam.retaillite.invoice.pdf.style.InvoiceTheme;
import in.shivam.retaillite.invoice.pdf.style.MoneyFormatter;
import org.springframework.stereotype.Component;

import java.awt.Color;

/**
 * Right-aligned summary box: subtotal and tax on a plain background, grand
 * total set apart on the theme's primary color - the visual anchor of the
 * page, matching the convention used by SAP/Dynamics-style invoices.
 */
@Component
public class TotalsSectionRenderer implements InvoiceSectionRenderer {

    @Override
    public void render(Document document, InvoiceDocument invoice, CompanyProperties company) throws DocumentException {
        PdfPTable wrapper = new PdfPTable(2);
        wrapper.setWidthPercentage(100);
        wrapper.setWidths(new float[]{6f, 4f});
        wrapper.setSpacingAfter(InvoiceTheme.SPACING_SECTION);

        PdfPCell leadingSpace = new PdfPCell();
        leadingSpace.setBorder(Rectangle.NO_BORDER);
        wrapper.addCell(leadingSpace);

        PdfPCell totalsCell = new PdfPCell();
        totalsCell.setBorder(Rectangle.NO_BORDER);
        totalsCell.addElement(buildTotalsTable(invoice));
        wrapper.addCell(totalsCell);

        document.add(wrapper);
    }

    private PdfPTable buildTotalsTable(InvoiceDocument invoice) {
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(100);
        totals.setWidths(new float[]{1f, 1f});

        addRow(totals, "Subtotal", MoneyFormatter.format(invoice.subtotal()), InvoiceTheme.FONT_BODY_MUTED, InvoiceTheme.FONT_BODY_BOLD, Color.WHITE);
        addRow(totals, "Tax", MoneyFormatter.format(invoice.tax()), InvoiceTheme.FONT_BODY_MUTED, InvoiceTheme.FONT_BODY_BOLD, Color.WHITE);
        addRow(totals, "Grand Total", MoneyFormatter.format(invoice.grandTotal()), InvoiceTheme.FONT_TOTALS_LABEL, InvoiceTheme.FONT_TOTALS_VALUE, InvoiceTheme.COLOR_TOTALS_BG);

        return totals;
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, Color background) {
        table.addCell(sideCell(label, labelFont, background, Element.ALIGN_LEFT));
        table.addCell(sideCell(value, valueFont, background, Element.ALIGN_RIGHT));
    }

    private PdfPCell sideCell(String text, Font font, Color background, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(background);
        cell.setPadding(InvoiceTheme.CELL_PADDING);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }
}
