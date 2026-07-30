package in.shivam.retaillite.invoice.pdf.section;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;

/**
 * Draws exactly one visual region of the invoice (header, parties, items table,
 * totals, payment details, ...) directly into the open {@link Document}.
 * <p>
 * Implementations must be stateless - {@link in.shivam.retaillite.invoice.pdf.generator.OpenPdfInvoiceGenerator}
 * holds them as Spring singletons and invokes them for every invoice generated.
 */
public interface InvoiceSectionRenderer {
    void render(Document document, InvoiceDocument invoice, CompanyProperties company) throws DocumentException;
}
