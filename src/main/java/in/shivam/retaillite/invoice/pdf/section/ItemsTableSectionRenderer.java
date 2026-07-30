package in.shivam.retaillite.invoice.pdf.section;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;
import in.shivam.retaillite.invoice.pdf.document.InvoiceItemDocument;
import in.shivam.retaillite.invoice.pdf.style.InvoiceTheme;
import in.shivam.retaillite.invoice.pdf.style.MoneyFormatter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.List;

/**
 * Renders the invoice line items as a bordered table with a shaded header row
 * and alternating row shading, matching the convention used by commercial ERP
 * invoice layouts.
 */
@Component
public class ItemsTableSectionRenderer implements InvoiceSectionRenderer {

    private static final float[] COLUMN_WIDTHS = {5f, 2f, 2.2f, 1.6f, 2.2f, 2.2f};
    private static final List<String> HEADERS = List.of("DESCRIPTION", "QTY", "UNIT PRICE", "TAX %", "TAX AMOUNT", "LINE TOTAL");

    @Override
    public void render(Document document, InvoiceDocument invoice, CompanyProperties company) throws DocumentException {
        PdfPTable table = new PdfPTable(COLUMN_WIDTHS.length);
        table.setWidthPercentage(100);
        table.setWidths(COLUMN_WIDTHS);
        table.setSpacingAfter(InvoiceTheme.SPACING_SECTION);

        addHeaderRow(table);
        addItemRows(table, invoice.items());

        document.add(table);

    }

    private void addHeaderRow(PdfPTable table) {
        HEADERS.forEach(label -> {
            PdfPCell cell = new PdfPCell(new Phrase(label, InvoiceTheme.FONT_TABLE_HEADER));
            cell.setBackgroundColor(InvoiceTheme.COLOR_TABLE_HEADER_BG);
            cell.setBorderColor(InvoiceTheme.COLOR_BORDER);
            cell.setPadding(InvoiceTheme.CELL_PADDING);
            cell.setHorizontalAlignment("DESCRIPTION".equals(label) ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            table.addCell(cell);
        });
    }

    private void addItemRows(PdfPTable table, List<InvoiceItemDocument> items) {
        boolean alternate = false;
        for (InvoiceItemDocument item : items) {
            Color rowColor = alternate ? InvoiceTheme.COLOR_TABLE_ALT_ROW : Color.WHITE;
            table.addCell(dataCell(nullSafe(item.productName()), Element.ALIGN_LEFT, rowColor));
            table.addCell(dataCell(String.valueOf(item.quantity()), Element.ALIGN_RIGHT, rowColor));
            table.addCell(dataCell(MoneyFormatter.format(item.unitPrice()), Element.ALIGN_RIGHT, rowColor));
            table.addCell(dataCell(formatPercentage(item.taxRate()), Element.ALIGN_RIGHT, rowColor));
            table.addCell(dataCell(MoneyFormatter.format(item.taxAmount()), Element.ALIGN_RIGHT, rowColor));
            table.addCell(dataCell(MoneyFormatter.format(item.lineTotal()), Element.ALIGN_RIGHT, rowColor));
            alternate = !alternate;
        }
    }

    private PdfPCell dataCell(String text, int alignment, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, InvoiceTheme.FONT_BODY));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(InvoiceTheme.CELL_PADDING);
        cell.setBackgroundColor(background);
        cell.setBorderColor(InvoiceTheme.COLOR_BORDER);
        return cell;
    }

    private String formatPercentage(BigDecimal rate) {
        return rate == null ? "\u2014" : rate.stripTrailingZeros().toPlainString() + "%";
    }

    private String nullSafe(String value) {
        return (value == null || value.isBlank()) ? "\u2014" : value;
    }
}
