package in.shivam.retaillite.invoice.pdf.generator;

import in.shivam.retaillite.invoice.config.CompanyProperties;
import in.shivam.retaillite.invoice.pdf.document.InvoiceDocument;

/**
 * Produces the final invoice PDF bytes from a rendering-ready {@link InvoiceDocument}
 * and the tenant's {@link CompanyProperties}.
 * <p>
 * This is the single seam between the invoice module and whichever PDF engine
 * is behind it. {@link OpenPdfInvoiceGenerator} is the current implementation;
 * swapping PDF libraries in the future means providing a new implementation of
 * this interface only.
 */
public interface PdfGenerator {
    byte[] generate(InvoiceDocument document, CompanyProperties company);
}
