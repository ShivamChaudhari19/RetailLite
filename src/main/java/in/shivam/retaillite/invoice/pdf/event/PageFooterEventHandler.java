package in.shivam.retaillite.invoice.pdf.event;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import in.shivam.retaillite.invoice.pdf.style.InvoiceTheme;

/**
 * Draws the page footer (rule, "computer-generated" note, and an accurate
 * "Page X of Y") on every page of the invoice.
 * <p>
 * This class holds per-document mutable state (the total-pages placeholder
 * template), so it must be instantiated once per PDF generation via
 * {@code new PageFooterEventHandler()} - it is intentionally <b>not</b> a
 * Spring-managed singleton, since {@link in.shivam.retaillite.invoice.pdf.generator.OpenPdfInvoiceGenerator}
 * may generate multiple invoices concurrently.
 */
public class PageFooterEventHandler extends PdfPageEventHelper {

    private static final float TOTAL_PAGES_PLACEHOLDER_WIDTH = 30f;

    private PdfTemplate totalPagesTemplate;

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        totalPagesTemplate = writer.getDirectContent().createTemplate(TOTAL_PAGES_PLACEHOLDER_WIDTH, 15f);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContent();
        float footerBaseline = document.bottom() - 25f;

        drawFooterRule(canvas, document, footerBaseline + 15f);
        drawFooterNote(canvas, document, footerBaseline);
        drawPageNumber(writer, canvas, document, footerBaseline);
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        totalPagesTemplate.beginText();
        totalPagesTemplate.setFontAndSize(InvoiceTheme.FONT_FOOTER.getBaseFont(), InvoiceTheme.FONT_FOOTER.getSize());
        totalPagesTemplate.showText(String.valueOf(writer.getPageNumber() - 1));
        totalPagesTemplate.endText();
    }

    private void drawFooterRule(PdfContentByte canvas, Document document, float y) {
        canvas.saveState();
        canvas.setLineWidth(0.5f);
        canvas.setColorStroke(InvoiceTheme.COLOR_BORDER);
        canvas.moveTo(document.left(), y);
        canvas.lineTo(document.right(), y);
        canvas.stroke();
        canvas.restoreState();
    }

    private void drawFooterNote(PdfContentByte canvas, Document document, float y) {
        Phrase note = new Phrase("Computer-generated invoice - no signature required", InvoiceTheme.FONT_FOOTER);
        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, note, document.left(), y, 0);
    }

    private void drawPageNumber(PdfWriter writer, PdfContentByte canvas, Document document, float y) {
        Phrase pageLabel = new Phrase("Page " + writer.getPageNumber() + " of ", InvoiceTheme.FONT_FOOTER);
        float labelWidth = ColumnText.getWidth(pageLabel);
        float startX = document.right() - labelWidth - TOTAL_PAGES_PLACEHOLDER_WIDTH;

        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, pageLabel, startX, y, 0);
        canvas.addTemplate(totalPagesTemplate, startX + labelWidth, y - 3f);
    }
}
